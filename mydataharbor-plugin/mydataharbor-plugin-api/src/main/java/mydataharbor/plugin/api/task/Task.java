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


package mydataharbor.plugin.api.task;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
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
   * 任务名称
   */
  private String taskName;

  /**
   * 任务描述
   */
  private String taskDescription;

  /**
   * 任务状态
   */
  private TaskState taskState = TaskState.created;

  /**
   * 此task使用哪个plugin创建pipeline
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

  /**
   * 业务自定义标签
   */
  private Map<String,String> tags  = new HashMap<>();


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Task task = (Task) o;
    return Objects.equals(taskId, task.taskId) && taskState == task.taskState;
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, taskState);
  }
}