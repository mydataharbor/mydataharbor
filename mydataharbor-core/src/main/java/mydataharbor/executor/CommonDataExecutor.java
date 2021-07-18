package mydataharbor.executor;

import mydataharbor.IDataPipline;

/**
 * 通用执行器
 *
 * @auth xulang
 * @Date 2021/5/8
 **/
public class CommonDataExecutor extends AbstractDataExecutor {
  public CommonDataExecutor(IDataPipline dataPipline, String threadName) {
    super(dataPipline, threadName);
  }
}
