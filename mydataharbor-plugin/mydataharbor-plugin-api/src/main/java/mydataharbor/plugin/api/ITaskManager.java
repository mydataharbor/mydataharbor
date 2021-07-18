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
   * @param numberOfPipline 目标
   * @param newSingleTask
   */
  void editTaskNum(String taskId, Integer numberOfPipline, SingleTask newSingleTask);
}
