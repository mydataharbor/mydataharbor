package mydataharbor.plugin.api.task;

import mydataharbor.constant.Constant;
import mydataharbor.util.RandomStringUtil;
import lombok.Data;

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
   * 单jvm pipline条数
   */
  private Integer numberOfPipline = 1;

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
    return Objects.equals(numberOfPipline, that.numberOfPipline);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), numberOfPipline);
  }

  @Override
  public String toString() {
    return "SingleTask [numberOfPipline=" + numberOfPipline + ", toString()=" + super.toString() + "]";
  }
}
