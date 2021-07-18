package mydataharbor.web.service.impl;

import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.exception.TaskManageException;
import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.util.PiplineStateUtil;
import mydataharbor.web.entity.TaskEditRequest;
import mydataharbor.web.service.INodeService;
import mydataharbor.web.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import mydataharbor.rpc.util.JsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
@Slf4j
@Component
public class TaskService implements ITaskService, InitializingBean {

  @Autowired
  private INodeService nodeService;

  private Map<String, DistributedTask> distributedTasks = new HashMap<>();

  @Override
  public TaskAssignedInfo submitTask(DistributedTask distributedTask) {
    //分配task id
    if (StringUtils.isBlank(distributedTask.getGroupName())) {
      throw new RuntimeException("提交的任务必须设置group");
    }
    if (StringUtils.isBlank(distributedTask.getPluginId())) {
      throw new RuntimeException("提交的任务必须设置pluginId");
    }
    GroupInfo cacheGroupInfo = nodeService.listGroupInfo().get(distributedTask.getGroupName());
    if (cacheGroupInfo == null) {
      throw new RuntimeException("没有该group");
    }
    List<PluginInfo> pluginInfos = cacheGroupInfo.getInstalledPlugins();
    PluginInfo pluginInfo = null;
    if (pluginInfos != null) {
      for (PluginInfo info : pluginInfos) {
        if (distributedTask.getPluginId().equals(info.getPluginId())) {
          pluginInfo = info;
          break;
        }
      }
    }
    if (pluginInfo == null) {
      throw new RuntimeException("该group没有安装该plugin，请检查！");
    }
    String nodeGroupPath = Constant.NODE_GROUP_PATH;
    String taskPathParent = Constant.TASK_PATH_PARENT;
    String taskId = distributedTask.generateTaskId();
    try {
      Stat stat = nodeService.getClient().checkExists().forPath(taskPathParent + taskId);
      while (stat != null) {
        //重新生成
        taskId = distributedTask.generateTaskId();
        stat = nodeService.getClient().checkExists().forPath(taskPathParent + taskId);
      }
    } catch (Exception e) {
      log.error("zk通信异常！", e);
      throw new RuntimeException("zk通信异常！", e);
    }

    distributedTask.setTaskId(taskId);
    //将任务平均分配到每台机器
    List<NodeInfo> nodeInfos = nodeService.lisNode().get(distributedTask.getGroupName());
    if (nodeInfos == null || nodeInfos.isEmpty()) {
      throw new RuntimeException("该组还没有机器，无法提交任务！");
    }
    //排序,taskNum 从小到大
    nodeInfos.sort((o1, o2) -> Long.valueOf(o1.getTaskNum().longValue()).compareTo(o2.getTaskNum().longValue()));
    TaskAssignedInfo taskAssignedInfo = new TaskAssignedInfo();
    taskAssignedInfo.setTaskId(distributedTask.getTaskId());
    for (Integer i = 0; i < distributedTask.getTotalNumberOfPipline(); i++) {
      int index = i % nodeInfos.size();
      NodeInfo nodeInfo = nodeInfos.get(index);
      TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(nodeInfo.getNodeName());
      if (nodeAssignedInfo == null) {
        nodeAssignedInfo = new TaskAssignedInfo.NodeAssignedInfo();
        nodeAssignedInfo.setNodeName(nodeInfo.getNodeName());
        taskAssignedInfo.getAssignedInfoMap().put(nodeInfo.getNodeName(), nodeAssignedInfo);
      }
      nodeAssignedInfo.setTaskNum(nodeAssignedInfo.getTaskNum() + 1);
    }
    distributedTask.setTaskAssignedInfo(taskAssignedInfo);
    //写入zk
    try {
      //写入task信息
      nodeService.getClient().create().creatingParentsIfNeeded().forPath(taskPathParent + taskId, JsonUtil.serialize(distributedTask));
      nodeService.groupTouch(distributedTask.getGroupName(), groupInfo -> groupInfo.getTasks().add(distributedTask.getTaskId()));
    } catch (Exception e) {
      log.error("向zk写入任务失败！");
      throw new RuntimeException("向zk写入任务失败！", e);
    }
    return taskAssignedInfo;
  }

  @Override
  public TaskState manageTaskState(String taskId, TaskState taskState) {
    while (true) {
      try {
        String taskPath = Constant.TASK_PATH_PARENT + taskId;
        Stat stat = new Stat();
        byte[] bytes = nodeService.getClient().getData().storingStatIn(stat).forPath(taskPath);
        DistributedTask distributedTask = JsonUtil.deserialize(bytes, DistributedTask.class);
        TaskState nowState = distributedTask.getTaskState();
        if (nowState.equals(taskState))
          return taskState;
        checkState(taskState, nowState, distributedTask);
        distributedTask.setTaskState(taskState);
        //写入zk
        nodeService.getClient().setData().withVersion(stat.getVersion()).forPath(taskPath, JsonUtil.serialize(distributedTask));
        //touch一下group，通知变更
        if (taskState == TaskState.over) {
          nodeService.groupTouch(distributedTask.getGroupName(), groupInfo -> groupInfo.getTasks().remove(taskId));
        } else {
          nodeService.groupTouch(distributedTask.getGroupName(), null);
        }
        break;
      } catch (KeeperException.BadVersionException badVersionException) {
        log.warn("乐观锁生效，重试更新...", badVersionException);
      } catch (Exception e) {
        throw new TaskManageException("更新状态发生异常：" + e.getMessage(), e);
      }
    }

    return taskState;
  }

  @Override
  public Map<String, DistributedTask> listTasks() {
    return distributedTasks;
  }

  @Override
  public Boolean editTask(TaskEditRequest taskEditRequest) {
    if (StringUtils.isBlank(taskEditRequest.getTaskId())) {
      throw new TaskManageException("taskid 必须！");
    }
    while (true) {
      try {
        String taskPath = Constant.TASK_PATH_PARENT + taskEditRequest.getTaskId();
        Stat stat = new Stat();
        byte[] bytes = nodeService.getClient().getData().storingStatIn(stat).forPath(taskPath);
        if (bytes == null || bytes.length == 0) {
          throw new TaskManageException("该任务不存在！");
        }
        DistributedTask distributedTask = JsonUtil.deserialize(bytes, DistributedTask.class);
        boolean hasEdit = false;
        if (taskEditRequest.getEnableRebalance() != null && !taskEditRequest.getEnableRebalance().equals(distributedTask.isEnableRebalance())) {
          distributedTask.setEnableRebalance(taskEditRequest.getEnableRebalance());
          hasEdit = true;
        }
        if (taskEditRequest.getTotalNumberOfPipline() != null && !taskEditRequest.getTotalNumberOfPipline().equals(distributedTask.getTotalNumberOfPipline())) {
          if (taskEditRequest.getTotalNumberOfPipline() < 0) {
            throw new TaskManageException("任务数不能小于0！");
          }
          if (taskEditRequest.getEnableRebalance() == false) {
            throw new TaskManageException("enableRebalance 为false时，不能修改pipline数！");
          }
          Map<String, TaskAssignedInfo.NodeAssignedInfo> assignedInfoMap = distributedTask.getTaskAssignedInfo().getAssignedInfoMap();
          if (assignedInfoMap != null || assignedInfoMap.size() != 0) {
            List<TaskAssignedInfo.NodeAssignedInfo> nodeAssignedInfos = assignedInfoMap.values().stream().collect(Collectors.toList());
            int change = taskEditRequest.getTotalNumberOfPipline() - distributedTask.getTotalNumberOfPipline();
            //将任务平均分配到每台机器
            List<NodeInfo> nodeInfos = nodeService.lisNode().get(distributedTask.getGroupName());
            if (nodeInfos == null || nodeInfos.isEmpty()) {
              throw new RuntimeException("该组还没有机器，无法提交任务！");
            }
            //排序,taskNum 从小到大
            nodeInfos.sort((o1, o2) -> Long.valueOf(o1.getTaskNum().longValue()).compareTo(o2.getTaskNum().longValue()));
            if (change > 0) {
              //增加
              for (int i = 0; i < change; i++) {
                int index = i % nodeInfos.size();
                TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = assignedInfoMap.get(nodeInfos.get(index).getNodeName());
                if (nodeAssignedInfo == null) {
                  nodeAssignedInfo = new TaskAssignedInfo.NodeAssignedInfo();
                  nodeAssignedInfo.setNodeName(nodeInfos.get(index).getNodeName());
                  assignedInfoMap.put(nodeInfos.get(index).getNodeName(), nodeAssignedInfo);
                }
                nodeAssignedInfo.addAndGetTaskNum(1);
              }
            } else if (taskEditRequest.getTotalNumberOfPipline() == 0) {
              assignedInfoMap.clear();
            } else {
              int leftChange = -change;
              int zeroCount = 0;
              while (leftChange != 0) {
                for (TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo : nodeAssignedInfos) {
                  if (nodeAssignedInfo.getTaskNum() > 0) {
                    nodeAssignedInfo.addAndGetTaskNum(-1);
                    leftChange--;
                  } else {
                    zeroCount++;
                  }
                }
                if (zeroCount == nodeAssignedInfos.size()) {
                  //容错
                  break;
                }
              }
            }
          }
          hasEdit = true;
          distributedTask.setTotalNumberOfPipline(taskEditRequest.getTotalNumberOfPipline());
        }
        if (!hasEdit) {
          return true;
        }
        //写入zk
        nodeService.getClient().setData().withVersion(stat.getVersion()).forPath(taskPath, JsonUtil.serialize(distributedTask));
        //通知任务变更
        nodeService.groupTouch(distributedTask.getGroupName(), null);
        return true;
      } catch (KeeperException.BadVersionException badVersionException) {
        log.warn("乐观锁生效，重试更新...", badVersionException);
      } catch (Exception e) {
        throw new TaskManageException("更新状态发生异常：" + e.getMessage(), e);
      }
    }
  }

  private void checkState(TaskState taskState, TaskState nowState, DistributedTask distributedTask) {
    if (nowState == TaskState.over) {
      throw new TaskManageException("该任务已经被销毁，禁止改变状态！");
    }
    switch (taskState) {
      case created:
        //创建
        throw new TaskManageException("创建请使用createPipline方法");
      case started:
        if (nowState != TaskState.created) {
          throw new TaskManageException("开始前状态不合法：当前状态" + nowState + "期望：" + TaskState.created);
        }
        //检查所有节点是否创建成功
        boolean doesAssignedTaskAllCreatedSuccess = PiplineStateUtil.doesAssignedTaskAllCreatedSuccess(distributedTask.getTaskAssignedInfo());
        if (!doesAssignedTaskAllCreatedSuccess) {
          throw new TaskManageException("有节点没有成功创建，无法启动，请检查！");
        }
        break;
      case suspend:
        if (nowState != TaskState.started && nowState != TaskState.continued) {
          throw new TaskManageException("当前状态不是正在运行，无法暂停");
        }
        break;
      case continued:
        if (nowState != TaskState.suspend) {
          throw new TaskManageException("当前状态不是暂停，无法执行继续操作");
        }
        break;
      case over:
        if (nowState == TaskState.over)
          throw new TaskManageException("该任务已经被销毁，不能修改状态");
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    PathChildrenCache pathChildrenCache = new PathChildrenCache(nodeService.getClient(), Constant.TASK_PATH_PARENT.substring(0, Constant.TASK_PATH_PARENT.length() - 1), true);
    pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
      @Override
      public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
        ChildData data = event.getData();
        DistributedTask distributedTask = JsonUtil.deserialize(data.getData(), DistributedTask.class);
        switch (event.getType()) {
          case CHILD_ADDED:
          case CHILD_UPDATED:
            distributedTasks.put(distributedTask.getTaskId(), distributedTask);
            break;
          case CHILD_REMOVED:
            distributedTasks.remove(distributedTask.getTaskId());
            break;
        }
      }
    });
    pathChildrenCache.start();
  }
}
