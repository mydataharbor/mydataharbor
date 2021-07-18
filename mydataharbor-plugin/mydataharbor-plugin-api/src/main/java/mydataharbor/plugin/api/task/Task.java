package mydataharbor.plugin.api.task;

import lombok.Data;

import java.util.Objects;

/**
 * task信息
 *
 * @auth xulang
 * @Date 2021/7/1
 **/
@Data
public class Task {
  /**
   * 任务号
   */
  private String taskId;
  /**
   * 任务状态
   */
  private TaskState taskState = TaskState.created;

  /**
   * 此task使用哪个plugin创建pipline
   */
  private String pluginId;

  /**
   * creator class
   */
  private String mydataharborCreatorClazz;

  /**
   * 配置的json形式
   */
  private String configJson;

  /**
   * setting的配置
   */
  private String settingJsonConfig;


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Task task = (Task) o;
    return Objects.equals(taskId, task.taskId) &&
      taskState == task.taskState;
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, taskState);
  }
}
