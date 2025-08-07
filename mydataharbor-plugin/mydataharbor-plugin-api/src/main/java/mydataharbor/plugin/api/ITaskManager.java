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

import mydataharbor.plugin.api.task.SingleTask;
import mydataharbor.plugin.api.task.TaskState;

import java.util.List;

/**
 * task管理器
 *
 * @auth xulang
 * @Date 2021/6/30
 **/
public interface ITaskManager {

  /**
   * 创建任务
   *
   * @param singleTask
   * @return
   */
  String submitTask(SingleTask singleTask);

  /**
   * 查询任务状态
   *
   * @param taskId
   * @return
   */
  TaskState queryTaskState(String taskId);

  /**
   * 列举任务
   *
   * @return
   */
  List<SingleTask> lisTask();

  /**
   * 管理任务状态
   *
   * @param taskId
   * @param taskState
   */
  void manageTask(String taskId, TaskState taskState);

  /**
   * 暂停并且启动
   */
  void pauseAndStart(String taskId);

  /**
   * 修改任务数量
   *
   * @param taskId
   * @param numberOfPipeline 目标
   * @param newSingleTask
   */
  void editTaskNum(String taskId, Integer numberOfPipeline, SingleTask newSingleTask);

    /**
     * 关闭
     */
  void close();
}