package mydataharbor.plugin.app.listener;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.IPluginRemoteManager;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.api.ITaskManager;
import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.SingleTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.rpc.util.JsonUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @auth xulang
 * @Date 2021/6/26
 **/
@Slf4j
public class GroupChangeListener implements NodeCacheListener {

  private NodeCache nodeCache;

  private IPluginRemoteManager pluginRemoteManager;

  private ITaskManager taskManager;

  private CuratorFramework client;

  private IPluginServer pluginServer;

  public GroupChangeListener(NodeCache nodeCache, IPluginRemoteManager pluginRemoteManager, ITaskManager taskManager, IPluginServer pluginServer, CuratorFramework client) {
    this.nodeCache = nodeCache;
    this.pluginRemoteManager = pluginRemoteManager;
    this.taskManager = taskManager;
    this.client = client;
    this.pluginServer = pluginServer;
  }

  @Override
  public synchronized void nodeChanged() throws Exception {
    log.info("收到group节点变化通知");
    ChildData currentData = nodeCache.getCurrentData();
    if (currentData == null || currentData.getData() == null || currentData.getData().length == 0) {
      return;
    }
    byte[] data = currentData.getData();
    GroupInfo groupInfo = JsonUtil.deserialize(data, GroupInfo.class);
    if (groupInfo != null) {
      try {
        groupInfoChangeProcessPlugin(groupInfo);
      } catch (Exception e) {
        log.error("处理plugin更新相关变更失败", e);
      }
      try {
        groupInfoChangeProcessTask(groupInfo);
      } catch (Exception e) {
        log.error("处理task相关变更失败！", e);
      }

    }
  }

  /**
   * grouping变更,task相关处理
   *
   * @param groupInfo
   */
  private void groupInfoChangeProcessTask(GroupInfo groupInfo) throws Exception {
    List<String> tasks = groupInfo.getTasks();
    List<DistributedTask> distributedTasks = new ArrayList<>();
    String taskPathParent = Constant.NODE_PREFIX + "/" + Constant.TASK_PATH + "/";
    for (String taskId : tasks) {
      byte[] bytes = client.getData().forPath(taskPathParent + taskId);
      DistributedTask distributedTask = JsonUtil.deserialize(bytes, DistributedTask.class);
      distributedTasks.add(distributedTask);
    }
    String nodeName = pluginServer.getNodeInfo().getNodeName();
    Map<String, SingleTask> newTaskMap = new HashMap<>();
    for (DistributedTask distributedTask : distributedTasks) {
      TaskAssignedInfo taskAssignedInfo = distributedTask.getTaskAssignedInfo();
      if (taskAssignedInfo != null && taskAssignedInfo.getAssignedInfoMap() != null) {
        TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(nodeName);
        if (nodeAssignedInfo != null) {
          if (nodeAssignedInfo.isDiverted()) {
            //如果本机任务因为网络原因被master已经被迁移，则直接停止
            taskManager.manageTask(distributedTask.getTaskId(), TaskState.over);
          } else {
            SingleTask singleTask = JsonUtil.deserialize(JsonUtil.serialize(distributedTask), SingleTask.class);
            singleTask.setNumberOfPipline(nodeAssignedInfo.getTaskNum());
            newTaskMap.put(singleTask.getTaskId(), singleTask);
          }

        }
      }
    }

    Map<String, SingleTask> runningTaskMap = taskManager.lisTask().stream().collect(Collectors.toMap(SingleTask::getTaskId, o -> o, (ke1, ke2) -> ke1));
    //本机增加任务
    for (Map.Entry<String, SingleTask> stringSingleTaskEntry : newTaskMap.entrySet()) {
      try {
        if (runningTaskMap.get(stringSingleTaskEntry.getKey()) == null
          && !stringSingleTaskEntry.getValue().getTaskState().equals(TaskState.over)) {
          log.info("增加任务:{}", stringSingleTaskEntry.getValue());
          taskManager.submitTask(stringSingleTaskEntry.getValue());
          //当前本机状态是created
          TaskState taskState = stringSingleTaskEntry.getValue().getTaskState();
          switch (taskState) {
            case created:
              //啥也不做，因为初始状态就是create
              break;
            case started:
            case continued:
              taskManager.manageTask(stringSingleTaskEntry.getKey(), TaskState.started);
              break;
            case suspend:
              //先启动，再暂停
              taskManager.pauseAndStart(stringSingleTaskEntry.getKey());
              break;
          }

        }
      } catch (Exception e) {
        log.error("增加任务:{},异常", stringSingleTaskEntry.getValue(), e);
      }

    }

    for (Map.Entry<String, SingleTask> stringSingleTaskEntry : runningTaskMap.entrySet()) {
      try {
        SingleTask newSingleTask = newTaskMap.get(stringSingleTaskEntry.getKey());
        if (newSingleTask == null) {
          //删除任务
          log.info("删除任务:{}", stringSingleTaskEntry.getKey());
          taskManager.manageTask(stringSingleTaskEntry.getKey(), TaskState.over);
        } else {
          if (!newSingleTask.equals(stringSingleTaskEntry.getValue())) {
            //任务修改
            //状态修改
            log.info("任务状态修改:{},{}", stringSingleTaskEntry.getKey(), newSingleTask.getTaskState());
            taskManager.manageTask(stringSingleTaskEntry.getKey(), newSingleTask.getTaskState());
            if (newSingleTask.getTaskState().equals(TaskState.over)) {
              continue;
            }
            //任务数修改
            log.info("任务数修改:{},新任务数:{}", stringSingleTaskEntry.getKey(), newSingleTask.getNumberOfPipline());
            taskManager.editTaskNum(stringSingleTaskEntry.getKey(), newSingleTask.getNumberOfPipline(), newSingleTask);
          }
        }
      } catch (Exception e) {
        log.error("任务修改发生异常！:{}", stringSingleTaskEntry.getValue(), e);
      }

    }


  }

  /**
   * groupinfo变更,plugin相关处理
   *
   * @param groupInfo
   */
  public void groupInfoChangeProcessPlugin(GroupInfo groupInfo) {
    Collector<PluginInfo, ?, Map<String, PluginInfo>> pluginInfoMapCollector = Collectors.toMap(PluginInfo::getPluginId, o -> o, (ke1, ke2) -> ke1);
    Map<String, PluginInfo> installedPlugins = groupInfo.getInstalledPlugins().stream().collect(pluginInfoMapCollector);
    List<PluginInfo> allPluginInfo = pluginRemoteManager.getAllPluginInfo();
    //比较
    Iterator<PluginInfo> iterator = allPluginInfo.iterator();
    while (iterator.hasNext()) {
      PluginInfo pluginInfo = iterator.next();
      if (installedPlugins.get(pluginInfo.getPluginId()) == null) {
        //卸载
        pluginRemoteManager.uninstallPlugin(pluginInfo.getPluginId());
        iterator.remove();
      }
    }
    Map<String, PluginInfo> allPluginInfoMap = allPluginInfo.stream().collect(pluginInfoMapCollector);
    for (PluginInfo installedPlugin : groupInfo.getInstalledPlugins()) {
      if (allPluginInfoMap.get(installedPlugin.getPluginId()) == null) {
        //安装
        pluginRemoteManager.loadPluginByRepository(installedPlugin.getPluginId(), installedPlugin.getVersion());
      }
    }
  }
}
