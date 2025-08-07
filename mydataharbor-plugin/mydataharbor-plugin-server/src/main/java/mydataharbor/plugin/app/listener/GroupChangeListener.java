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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

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
            singleTask.setNumberOfPipeline(nodeAssignedInfo.getTaskNum());
            newTaskMap.put(singleTask.getTaskId(), singleTask);
          }

        }
      }
    }

    Map<String, SingleTask> runningTaskMap = taskManager.lisTask().stream().collect(Collectors.toMap(SingleTask::getTaskId, o -> o, (ke1, ke2) -> ke1));
    //本机增加任务
    for (Map.Entry<String, SingleTask> newTaskEntry : newTaskMap.entrySet()) {
      try {
        if (runningTaskMap.get(newTaskEntry.getKey()) == null  && !newTaskEntry.getValue().getTaskState().equals(TaskState.over)) {
          log.info("增加任务:{}", newTaskEntry.getValue());
          taskManager.submitTask(newTaskEntry.getValue());
          //当前本机状态是created
          TaskState taskState = newTaskEntry.getValue().getTaskState();
          switch (taskState) {
            case created:
              //啥也不做，因为初始状态就是create
              break;
            case started:
            case continued:
              taskManager.manageTask(newTaskEntry.getKey(), TaskState.started);
              break;
            case suspend:
              //先启动，再暂停
              taskManager.pauseAndStart(newTaskEntry.getKey());
              break;
          }
        }
      } catch (Exception e) {
        log.error("增加任务:{},异常", newTaskEntry.getValue(), e);
      }

    }

    for (Map.Entry<String, SingleTask> runningTaskEntry : runningTaskMap.entrySet()) {
      try {
        SingleTask newSingleTask = newTaskMap.get(runningTaskEntry.getKey());
        if (newSingleTask == null) {
          //删除任务
          log.info("删除任务:{}", runningTaskEntry.getKey());
          taskManager.manageTask(runningTaskEntry.getKey(), TaskState.over);
        } else {
          if (!newSingleTask.equals(runningTaskEntry.getValue())) {
            //任务修改
            //状态修改
            log.info("任务状态修改:{},{}", runningTaskEntry.getKey(), newSingleTask.getTaskState());
            taskManager.manageTask(runningTaskEntry.getKey(), newSingleTask.getTaskState());
            if (newSingleTask.getTaskState().equals(TaskState.over)) {
              continue;
            }
            //任务数修改
            log.info("任务数修改:{},新任务数:{}", runningTaskEntry.getKey(), newSingleTask.getNumberOfPipeline());
            taskManager.editTaskNum(runningTaskEntry.getKey(), newSingleTask.getNumberOfPipeline(), newSingleTask);
          }
        }
      } catch (Exception e) {
        log.error("任务修改发生异常！:{}", runningTaskEntry.getValue(), e);
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
    List<String> loadedPlugins = new ArrayList<>();
    for (PluginInfo installedPlugin : groupInfo.getInstalledPlugins()) {
        PluginInfo nowInstallPluginInfo = allPluginInfoMap.get(installedPlugin.getPluginId());
        if (nowInstallPluginInfo == null) {
        //安装
          try {
              String loadedPlugin = pluginRemoteManager.loadPluginByRepository(installedPlugin.getPluginId(), installedPlugin.getVersion());
              if (loadedPlugin != null) {
                  loadedPlugins.add(loadedPlugin);
              }
          }catch (Throwable e){
              log.error("加载插件失败",e);
          }
      } else if(!nowInstallPluginInfo.getVersion().equals(installedPlugin.getVersion())){
            //版本变更，先卸载再安装
            pluginRemoteManager.uninstallPlugin(nowInstallPluginInfo.getPluginId());
            //安装
            try {
                String loadedPlugin = pluginRemoteManager.loadPluginByRepository(installedPlugin.getPluginId(), installedPlugin.getVersion());
                if (loadedPlugin != null) {
                    loadedPlugins.add(loadedPlugin);
                }
            }catch (Throwable e){
                log.error("加载插件失败",e);
            }
      }
    }
    for (String loadedPlugin : loadedPlugins) {
      pluginRemoteManager.startPlugin(loadedPlugin);
    }
  }
}