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


package mydataharbor.setting;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;

/**
 * 启动器基础全局配置
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
@Data
@SuperBuilder
@MyDataHarborMarker(title = "启动器基础全局配置")
public class BaseSettingContext {

  public BaseSettingContext() {

  }

  /**
   * 对于每一个pipeline是否开启并行处理
   */
  @Builder.Default
  @MyDataHarborMarker(title = "对于每一个pipeline是否开启并行处理", defaultValue = "false")
  private boolean parallel = false;

  /**
   * 如果开启并行，最大线程数，0采用java默认线程池执行
   */
  @Builder.Default
  @MyDataHarborMarker(title = "ForkJoin最大线程数", des = "如果开启并行，最大线程数，0采用java默认线程池执行", defaultValue = "0")
  private int threadNum = 0;

  /**
   * 是否批量写入
   * 批量写入的前提下肯定是批量提交
   */
  @MyDataHarborMarker(title = "是否批量写入", des = "true批量写入，false单条写入", defaultValue = "false")
  private boolean batchWrite;

  /**
   * 在单条写入的情况下，是否需要批量提交
   */
  @MyDataHarborMarker(title = "是否需要批量提交", des = "在单条写入的情况下可以设置是否批量提交，批量写入下该参数不起效果（批量写入下只能批量提交）", defaultValue = "true")
  private boolean batchCommit = true;

  /**
   * 每次poll的休息时间
   */
  @MyDataHarborMarker(title = "每次poll的休息时间", defaultValue = "0")
  private long sleepTime = 0L;

  /**
   * 在一次poll中，同rollback事务单元，当有rollback发生的时候，是否还继续执行
   */
  @MyDataHarborMarker(title = "当有rollback发生的时候，是否还继续执行", des = "在一次poll中，同rollback事务单元，当有rollback发生的时候，是否还继续执行", defaultValue = "true")
  private boolean continueOnRollbackOccurContinueInOncePoll = true;
}