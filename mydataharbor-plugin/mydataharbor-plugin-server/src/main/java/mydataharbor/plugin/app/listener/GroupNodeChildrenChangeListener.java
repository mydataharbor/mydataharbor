package mydataharbor.plugin.app.listener;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.monitor.ExecutorCrash;
import mydataharbor.plugin.api.IRebalance;
import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.rpc.util.JsonUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @auth xulang
 * @Date 2021/7/3
 **/
@Slf4j
public class GroupNodeChildrenChangeListener implements PathChildrenCacheListener {

  private NodeInfo nodeInfo;

  private IRebalance rebalance;

  public GroupNodeChildrenChangeListener(NodeInfo nodeInfo, IRebalance rebalance) {
    this.nodeInfo = nodeInfo;
    this.rebalance = rebalance;
  }

  @Override
  public synchronized void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
    if (event.getType() != PathChildrenCacheEvent.Type.CHILD_ADDED && event.getType() != PathChildrenCacheEvent.Type.CHILD_REMOVED) {
      return;
    }
    //等待1s，避免重置太频繁
    Thread.sleep(1000);
    NodeInfo changeNode = JsonUtil.deserialize(event.getData().getData(), NodeInfo.class);
    if (!nodeInfo.isLeader()) {
      log.debug("有节点变化，但是我不是leader，我不处理！");
      return;
    } else {
      log.debug("我是leader，我来处理...");
    }
    byte[] data = client.getData().forPath(Constant.NODE_GROUP_PATH + nodeInfo.getGroup());
    if (data == null || data.length == 0)
      return;
    GroupInfo groupInfo = JsonUtil.deserialize(data, GroupInfo.class);
    List<String> tasks = groupInfo.getTasks();
    List<DistributedTask> rebalanceTaskList = new ArrayList<>();
    List<DistributedTask> disableRebalanceTaskList = new ArrayList<>();
    for (String task : tasks) {
      byte[] taskData = client.getData().forPath(Constant.TASK_PATH_PARENT + task);
      DistributedTask distributedTask = JsonUtil.deserialize(taskData, DistributedTask.class);
      if (distributedTask.getTaskState() != TaskState.over) {
        if (distributedTask.isEnableRebalance()) {
          rebalanceTaskList.add(distributedTask);
        } else {
          disableRebalanceTaskList.add(distributedTask);
          log.warn("该任务设置了不转移：{}", distributedTask);
        }
      }
    }
    //获取现在组内还有的机器信息
    List<String> nodeChildren = client.getChildren().forPath(Constant.NODE_GROUP_PATH + nodeInfo.getGroup());
    List<NodeInfo> liveNodes = new ArrayList<>();
    for (String nodeChild : nodeChildren) {
      byte[] nodeData = client.getData().forPath(Constant.NODE_GROUP_PATH + nodeInfo.getGroup() + "/" + nodeChild);
      NodeInfo liveNode = JsonUtil.deserialize(nodeData, NodeInfo.class);
      liveNodes.add(liveNode);
    }
    boolean hasChange = false;
    try {
      switch (event.getType()) {
        case CHILD_ADDED:
          //与新机器加入
          log.info("node join，开始Rebalance以下任务:{}", rebalanceTaskList);
          for (DistributedTask distributedTask : rebalanceTaskList) {
            TaskAssignedInfo taskAssignedInfo = rebalance.rebalance(liveNodes, distributedTask, changeNode, true);
            if (taskAssignedInfo != null) {
              //写入
              hasChange = true;
              changeTaskAssignedInfo(client, taskAssignedInfo, distributedTask.getTaskId());
            }
          }
          break;
        case CHILD_REMOVED:
          //有机器离开
          log.info("node leve,开始Rebalance以下任务:{}", rebalanceTaskList);
          //检查此次离开的节点是否有在运行无法Rebalance的任务上
          for (DistributedTask distributedTask : disableRebalanceTaskList) {
            TaskAssignedInfo taskAssignedInfo = distributedTask.getTaskAssignedInfo();
            Map<String, TaskAssignedInfo.NodeAssignedInfo> assignedInfoMap = taskAssignedInfo.getAssignedInfoMap();
            for (Map.Entry<String, TaskAssignedInfo.NodeAssignedInfo> stringNodeAssignedInfoEntry : assignedInfoMap.entrySet()) {
              if (changeNode.getNodeName().equals(stringNodeAssignedInfoEntry.getKey())) {
                //消失的节点上有在运行无法Rebalance的任务
                TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = stringNodeAssignedInfoEntry.getValue();
                new ExecutorCrash(distributedTask.getTaskId(), changeNode.getNodeName(), changeNode.getIp(), changeNode.getPort(), nodeAssignedInfo.getTaskNum());
                log.error("消失的节点上有正在运行无法Rebalance的任务，任务id:{},该节点任务信息:{}", distributedTask.getTaskId(), nodeAssignedInfo);
              }
            }
          }
          for (DistributedTask distributedTask : rebalanceTaskList) {
            TaskAssignedInfo taskAssignedInfo = rebalance.rebalance(liveNodes, distributedTask, changeNode, false);
            //写入
            hasChange = true;
            if (taskAssignedInfo != null) {
              hasChange = true;
              changeTaskAssignedInfo(client, taskAssignedInfo, distributedTask.getTaskId());
            }
          }

          break;
      }
    } finally {
      if (hasChange) {
        groupTouch(client, groupInfo.getGroupName(), null);
      }
    }

  }

  public void groupTouch(CuratorFramework client, String groupName, Consumer<GroupInfo> groupChangeAction) {
    //查询group信息
    String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + groupName;
    try {
      while (true) {
        try {
          Stat stat = new Stat();
          byte[] data = client.getData().storingStatIn(stat).forPath(groupPath);
          if (groupChangeAction != null) {
            GroupInfo groupInfo = JsonUtil.deserialize(data, GroupInfo.class);
            groupChangeAction.accept(groupInfo);
            client.setData().withVersion(stat.getVersion()).forPath(groupPath, JsonUtil.serialize(groupInfo));
          } else {
            client.setData().withVersion(stat.getVersion()).forPath(groupPath, data);
          }
          break;
        } catch (KeeperException.BadVersionException e) {
          log.warn("乐观锁重试", e);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("zk操作失败！", e);
    }
  }


  private void changeTaskAssignedInfo(CuratorFramework client, TaskAssignedInfo newTaskAssignedInfo, String taskId) {
    if (client != null) {
      while (true) {
        try {
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(Constant.TASK_PATH_PARENT + taskId);
          DistributedTask distributedTask = JsonUtil.deserialize(bytes, DistributedTask.class);
          distributedTask.setTaskAssignedInfo(newTaskAssignedInfo);
          client.setData().withVersion(stat.getVersion()).forPath(Constant.TASK_PATH_PARENT + taskId, JsonUtil.serialize(distributedTask));
          break;
        } catch (KeeperException.BadVersionException badVersionException) {
          log.warn("乐观锁生效，重试..");
        } catch (Exception exception) {
          log.error("更新状态发生异常！", exception);
          break;
        }
      }
    }
  }

}
