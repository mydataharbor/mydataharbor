package mydataharbor.monitor;

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

  /**
   * 协议数据转换成功数
   *
   * @return
   */
  Long getProtocalConventSuccessCount();

  /**
   * 协议转换失败记录
   *
   * @return
   */
  Long getProtocalConventErrorCount();

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

  /**
   * 数据转换通过记录
   *
   * @return
   */
  Long getDataConventSuccessCount();

  /**
   * 数据转换失败记录
   *
   * @return
   */
  Long getDataConventErrorCount();

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

  /**
   * 获取最后运行时间
   *
   * @return
   */
  Long getLastRunTime();

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

  void addAndGettRecordCount(Long change);

  void addAndGetProtocalConventSuccessCount(Long change);

  void addAndGetProtocalConventErrorCount(Long change);

  void addAndGetCheckerSuccessCount(Long change);

  void addAndGetCheckerErrorCount(Long change);

  void addAndGetDataConventSuccessCount(Long change);

  void addAndGetDataConventErrorCount(Long change);

  void addAndGetWriteSuccessCount(Long change);

  void addAndGetWriteErrorCount(Long change);

  void setLastRunTime(Long lastRunTime);

  void setTotal(Long total);

}
