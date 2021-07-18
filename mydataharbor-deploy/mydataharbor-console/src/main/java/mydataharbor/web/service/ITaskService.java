package mydataharbor.web.service;

import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.web.entity.TaskEditRequest;

import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
public interface ITaskService {

  /**
   * 提交任务
   *
   * @param distributedTask
   * @return
   */
  TaskAssignedInfo submitTask(DistributedTask distributedTask);

  /**
   * 管理任务的状态
   *
   * @param taskId
   * @param taskState
   * @return
   */
  TaskState manageTaskState(String taskId, TaskState taskState);

  /**
   * 获取所有任务
   *
   * @return
   */
  Map<String, DistributedTask> listTasks();

  /**
   * 修改任务
   * @param taskEditRequest
   * @return
   */
  Boolean editTask(TaskEditRequest taskEditRequest);
}
