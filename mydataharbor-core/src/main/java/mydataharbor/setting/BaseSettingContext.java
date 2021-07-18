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
   * 是否批量，写入/提交
   * 如果是非批量提交，需要数据源支持单条提交
   */
  private boolean batch;

  /**
   * 每次poll的休息时间
   */
  private long sleepTime = 0L;

  /**
   * 在一次poll中，同rollback事务单元，当有rollback发生的时候，是否还继续执行
   */
  private boolean continueOnRollbackOccurContinueInOncePoll = true;
}
