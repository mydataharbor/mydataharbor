package mydataharbor.plugin.sink.es;


import mydataharbor.sink.AbstractEsSink;
import mydataharbor.sink.EsSinkConfig;
import mydataharbor.sink.es.IEsClient;

/**
 * Created by xulang on 2021/7/27.
 */
public class Es68xSink extends AbstractEsSink {
  public Es68xSink(EsSinkConfig esSinkConfig) {
    super(esSinkConfig);
  }

  @Override
  public IEsClient initEsClient(EsSinkConfig esSinkConfig) {
    return new Es68xClient(esSinkConfig);
  }

  @Override
  public String name() {
    return "es6.8.x 版本的写入器";
  }
}
