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
import mydataharbor.constant.Constant;
import mydataharbor.util.RandomStringUtil;

import java.util.Objects;

/**
 * 单机运行task信息
 *
 * @auth xulang
 * @Date 2021/6/23
 **/
@Data
public class SingleTask extends Task {
  /**
   * 单jvm pipeline条数
   */
  private Integer numberOfPipeline = 1;

  public String generateTaskId() {
    return Constant.TASK_PATH + "-" + RandomStringUtil.generateRandomStr(10);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    if (!super.equals(o))
      return false;
    SingleTask that = (SingleTask) o;
    return Objects.equals(numberOfPipeline, that.numberOfPipeline);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), numberOfPipeline);
  }

  @Override
  public String toString() {
    return "SingleTask [numberOfPipeline=" + numberOfPipeline + ", toString()=" + super.toString() + "]";
  }
}