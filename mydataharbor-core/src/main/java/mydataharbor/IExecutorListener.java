package mydataharbor;

import mydataharbor.executor.AbstractDataExecutor;

/**
 * 线程执行过中的监听器
 *
 * @auth xulang
 * @Date 2021/7/2
 **/
public interface IExecutorListener {

  /**
   * 执行创建后执行
   *
   * @param throwable
   */
  void onPiplineCreate(int begin, int change, Throwable throwable);

  /**
   * 运行
   *
   * @param executor
   */
  void onRun(AbstractDataExecutor executor, IDataPipline pipline);

  /**
   * 暂停
   *
   * @param executor
   */
  void onSuspend(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal);

  /**
   * 继续
   *
   * @param executor
   */
  void onContinue(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal);

  /**
   * 和平结束
   *
   * @param executor
   * @param pipline
   */
  void onSucccessEnd(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal, boolean isRun);


  /**
   * 关闭
   * @param executor
   * @param pipline
   * @param writeTotal
   * @param isRun
   */
  void onClose(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal,boolean isRun);

  /**
   * 异常退出
   *
   * @param executor
   * @param pipline
   * @param throwable
   */
  void onExceptionEnd(AbstractDataExecutor executor, IDataPipline pipline, Throwable throwable, long writeTotal);


}
