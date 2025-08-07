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


package mydataharbor.monitor;

import mydataharbor.pipeline.PipelineState;

/**
 * 每一个线程的监控
 * @auth xulang
 * @Date 2021/7/14
 **/
public interface TaskExecutorMonitorMBean {

  /**
   * 拉取的原始消息总数
   *
   * @return
   */
  Long getTRecordCount();

  Long getTRecordUseTime();

  /**
   * 协议数据转换成功数
   *
   * @return
   */
  Long getProtocolConvertSuccessCount();

  /**
   * 协议转换失败记录
   *
   * @return
   */
  Long getProtocolConvertErrorCount();

  Long getProtocolConvertUseTime();

  /**
   * 检查通过记录
   *
   * @return
   */
  Long getCheckerSuccessCount();

  /**
   * 检查失败记录
   *
   * @return
   */
  Long getCheckerErrorCount();

  Long getCheckerUseTime();

  /**
   * 数据转换通过记录
   *
   * @return
   */
  Long getDataConvertSuccessCount();

  /**
   * 数据转换失败记录
   *
   * @return
   */
  Long getDataConvertErrorCount();

  Long getDataConvertUseTime();

  /**
   * 写入成功记录
   *
   * @return
   */
  Long getWriteSuccessCount();

  /**
   * 写入失败记录
   *
   * @return
   */
  Long getWriteErrorCount();

  Long getWriteUseTime();

  /**
   * 获取最后运行时间
   *
   * @return
   */
  Long getLastRunTime();

  /**
   * 写入耗时
   * @return
   */
  Long getUseTime();

  /**
   * 获取总数
   * @return
   */
  Long getTotal();

  /**
   * 执行
   * @return
   */
  boolean isRun();

  /**
   * 暂停
   * @return
   */
  boolean isSuspend();

  /**
   * 线程是否结束
   * @return
   */
  boolean isEnd();

  /**
   * 状态
   * @return
   */
  PipelineState getPipelineSate();

  void addAndGettRecordCount(Long change);

  void addAndGetProtocolConvertSuccessCount(Long change);

  void addAndGetProtocolConvertErrorCount(Long change);

  void addAndGetCheckerSuccessCount(Long change);

  void addAndGetCheckerErrorCount(Long change);

  void addAndGetDataConvertSuccessCount(Long change);

  void addAndGetDataConvertErrorCount(Long change);

  void addAndGetWriteSuccessCount(Long change);

  void addAndGetWriteErrorCount(Long change);

  void setLastRunTime(Long lastRunTime);

  void setTotal(Long total);

}