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


package mydataharbor.web.service;

import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.web.dto.resp.TaskMonitorInfo;
import mydataharbor.web.entity.RecreateTaskRequest;
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

    /**
     * 任务重建
     * @param recreateTaskRequest
     * @return
     */
    Boolean recreateTask(RecreateTaskRequest recreateTaskRequest);

  /**
   * 删除任务
   * @return
   */
  Boolean deleteTask(String taskId);

    /**
     * 获取任务监控信息
     * @param taskId
     * @return
     */
  TaskMonitorInfo getTaskMonitorInfo(String taskId);

}