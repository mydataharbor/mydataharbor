/*
 * 版权所有 (C) [2020] [xulang 1053618636@qq.com]
 *
 * 此程序是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证第3版或
 * （根据您的选择）任何更高版本重新分发和/或修改它。
 *
 * 此程序基于希望它有用而分发，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。详见 GNU 通用公共许可证。
 *
 * 您应该已经收到 GNU 通用公共许可证的副本。如果没有，请参阅
 * <http://www.gnu.org/licenses/>.
 *
 */


package mydataharbor.plugin.app.rebalance;

import mydataharbor.plugin.api.IRebalance;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    Integer totalNumberOfPipeline = distributedTask.getTotalNumberOfPipeline();
    if (distributedTask.isEnableLoadBalance() || liveNodes.size() == 1) {
      List<NodeInfo> newLiveNodes = new ArrayList<>(liveNodes);
      //到排序
      newLiveNodes.sort((o1, o2) -> Long.valueOf(o2.getTaskNum().longValue()).compareTo(o1.getTaskNum().longValue()));
      //每台理想状态下的任务数
      int avg = totalNumberOfPipeline / liveNodes.size();
      avg = avg == 0 ? 1 : avg;
      int notDistribution = totalNumberOfPipeline;
      for (NodeInfo newLiveNode : newLiveNodes) {
        if (notDistribution > 0) {
          TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(newLiveNode.getNodeName());
          if (nodeAssignedInfo == null) {
            nodeAssignedInfo = new TaskAssignedInfo.NodeAssignedInfo();
            nodeAssignedInfo.setNodeName(newLiveNode.getNodeName());
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