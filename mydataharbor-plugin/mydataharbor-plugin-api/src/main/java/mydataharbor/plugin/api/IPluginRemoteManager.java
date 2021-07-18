package mydataharbor.plugin.api;

import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.task.SingleTask;
import mydataharbor.plugin.api.task.TaskState;
import org.pf4j.PluginState;

import java.util.List;

/**
 * 远程操作接口
 *
 * @auth xulang
 * @Date 2021/6/17
 **/
public interface IPluginRemoteManager {

  /**
   * 关闭服务
   */
  void stop();

  /**
   * 通过rpc直接传送插件文件
   *
   * @return
   */
  String loadPluginByRpc(String pluginFileName, byte[] body);

  /**
   * 通过插件仓库安装
   *
   * @param pluginId
   * @return
   */
  String loadPluginByRepository(String pluginId, String version);

  /**
   * 移除插件
   *
   * @param pluginId
   * @return
   */
  boolean uninstallPlugin(String pluginId);

  /**
   * 启动插件
   *
   * @param pluginId
   * @return
   */
  PluginState startPlugin(String pluginId);

  /**
   * 获取所有plugin信息
   *
   * @return
   */
  List<PluginInfo> getAllPluginInfo();


  /**
   * 通过插件id获得安装信息
   *
   * @return
   */
  PluginInfo getPluginInfoByPluginId(String pluginId);


  /**
   * 创建pipline
   *
   * @param singleTask 任务信息
   * @return taskid
   */
  String createPipline(SingleTask singleTask);

  /**
   * 管理task
   *
   * @param taskId
   */
  void manageTask(String taskId, TaskState taskState);

  /**
   * 查询任务状态
   *
   * @param taskId
   * @return
   */
  TaskState queryTaskState(String taskId);

  /**
   * 枚举所有task
   *
   * @return
   */
  List<SingleTask> lisTask();
}
