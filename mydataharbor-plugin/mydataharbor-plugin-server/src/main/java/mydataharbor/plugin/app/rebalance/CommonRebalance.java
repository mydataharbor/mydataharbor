package mydataharbor.plugin.app.rebalance;

import mydataharbor.plugin.api.IRebalance;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 平均Rebalance算法
 *
 * @auth xulang
 * @Date 2021/7/3
 **/
public class CommonRebalance implements IRebalance {

  private boolean ifContain(List<NodeInfo> liveNodes, String nodeName) {
    for (NodeInfo liveNode : liveNodes) {
      if (liveNode.getNodeName().equals(nodeName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public TaskAssignedInfo rebalance(List<NodeInfo> liveNodes, DistributedTask distributedTask, NodeInfo changeNode, boolean join) {
    TaskAssignedInfo taskAssignedInfo = null;
    //清理已经下线的机器任务
    Iterator<Map.Entry<String, TaskAssignedInfo.NodeAssignedInfo>> iterator = distributedTask.getTaskAssignedInfo().getAssignedInfoMap().entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, TaskAssignedInfo.NodeAssignedInfo> next = iterator.next();
      if (!ifContain(liveNodes, next.getKey()) || next.getValue().isDiverted()) {
        if (!next.getKey().equals(changeNode.getNodeName())) {
          iterator.remove();
        }
      }
    }
    if (!join) {
      taskAssignedInfo = changeNodeOnNodeLeve(liveNodes, distributedTask, changeNode);
    } else {
      taskAssignedInfo = changeNodeOnNodeJoin(liveNodes, distributedTask, changeNode);
    }
    return taskAssignedInfo;
  }

  private TaskAssignedInfo changeNodeOnNodeJoin(List<NodeInfo> liveNodes, DistributedTask distributedTask, NodeInfo joinNode) {
    //机器加入
    TaskAssignedInfo taskAssignedInfo = distributedTask.getTaskAssignedInfo();
    Integer totalNumberOfPipline = distributedTask.getTotalNumberOfPipline();
    if (totalNumberOfPipline > 2 * liveNodes.size() || liveNodes.size() == 1) {
      TaskAssignedInfo taskAssignedInfo1 = distributedTask.getTaskAssignedInfo();
      //任务数是目前机器数的2倍再转移
      List<NodeInfo> newLiveNodes = new ArrayList<>(liveNodes);
      //到排序
      newLiveNodes.sort((o1, o2) -> Long.valueOf(o2.getTaskNum().longValue()).compareTo(o1.getTaskNum().longValue()));
      //每台理想状态下的任务数
      int avg = totalNumberOfPipline / liveNodes.size();
      avg = avg == 0 ? 1 : avg;
      int notDistribution = totalNumberOfPipline;
      for (NodeInfo newLiveNode : newLiveNodes) {
        if (notDistribution > 0) {
          TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(newLiveNode.getNodeName());
          if (nodeAssignedInfo == null) {
            nodeAssignedInfo = new TaskAssignedInfo.NodeAssignedInfo();
            nodeAssignedInfo.setNodeName(joinNode.getNodeName());
            taskAssignedInfo.getAssignedInfoMap().put(newLiveNode.getNodeName(), nodeAssignedInfo);
          }
          nodeAssignedInfo.setTaskNum(avg);
          notDistribution -= avg;
        }
      }
      if (notDistribution > 0) {
        //分配给新加入的节点
        TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(joinNode.getNodeName());
        if (nodeAssignedInfo == null) {
          nodeAssignedInfo = new TaskAssignedInfo.NodeAssignedInfo();
          nodeAssignedInfo.setNodeName(joinNode.getNodeName());
          taskAssignedInfo.getAssignedInfoMap().put(joinNode.getNodeName(), nodeAssignedInfo);
        }
        nodeAssignedInfo.setTaskNum(nodeAssignedInfo.getTaskNum() + notDistribution);
      }
    }
    return taskAssignedInfo;
  }

  public TaskAssignedInfo changeNodeOnNodeLeve(List<NodeInfo> liveNodes, DistributedTask distributedTask, NodeInfo leveNode) {
    TaskAssignedInfo taskAssignedInfo = distributedTask.getTaskAssignedInfo();
    Map<String, TaskAssignedInfo.NodeAssignedInfo> assignedInfoMap = taskAssignedInfo.getAssignedInfoMap();
    TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = assignedInfoMap.get(leveNode.getNodeName());
    if (nodeAssignedInfo == null || nodeAssignedInfo.isDiverted()) {
      return null;
    }
    //遗弃
    assignedInfoMap.remove(leveNode.getNodeName());
    List<NodeInfo> newLiveNodes = new ArrayList<>(liveNodes);
    //排序
    newLiveNodes.sort((o1, o2) -> Long.valueOf(o1.getTaskNum().longValue()).compareTo(o2.getTaskNum().longValue()));
    //需要转移的任务数
    Integer taskNum = nodeAssignedInfo.getTaskNum();
    for (Integer i = 0; i < taskNum; i++) {
      int index = i % newLiveNodes.size();
      NodeInfo nodeInfo = newLiveNodes.get(index);
      TaskAssignedInfo.NodeAssignedInfo selectNodeAssignedInfo = assignedInfoMap.get(nodeInfo.getNodeName());
      if (selectNodeAssignedInfo == null) {
        selectNodeAssignedInfo = new TaskAssignedInfo.NodeAssignedInfo();
        selectNodeAssignedInfo.setNodeName(nodeInfo.getNodeName());
        assignedInfoMap.put(nodeInfo.getNodeName(), selectNodeAssignedInfo);
      }
      selectNodeAssignedInfo.setTaskNum(selectNodeAssignedInfo.getTaskNum() + 1);
    }
    return taskAssignedInfo;
  }


}
