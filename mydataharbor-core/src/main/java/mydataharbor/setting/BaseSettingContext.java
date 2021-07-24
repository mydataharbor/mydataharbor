package mydataharbor.setting;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 启动器基础全局配置
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
@Data
@SuperBuilder
public class BaseSettingContext {

  public BaseSettingContext() {

  }

  /**
   * 对于每一个pipline是否开启并行处理
   */
  @Builder.Default
  private boolean parallel = false;

  /**
   * 如果开启并行，最大线程数，0采用java默认线程池执行
   */
  @Builder.Default
  private int threadNum = 0;

  /**
   * 是否批量写入
   * 批量写入的前提下肯定是批量提交
   */
  private boolean batchWrite;

  /**
   * 在单条写入的情况下，是否需要批量提交
   */
  private boolean batchCommit;

  /**
   * 每次poll的休息时间
   */
  private long sleepTime = 0L;

  /**
   * 在一次poll中，同rollback事务单元，当有rollback发生的时候，是否还继续执行
   */
  private boolean continueOnRollbackOccurContinueInOncePoll = true;
}
