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


package mydataharbor.plugin.api;

import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.task.SingleTask;
import mydataharbor.plugin.api.task.TaskState;

import java.util.List;

import org.pf4j.PluginState;

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
   * 创建pipeline
   *
   * @param singleTask 任务信息
   * @return taskid
   */
  String createPipeline(SingleTask singleTask);

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