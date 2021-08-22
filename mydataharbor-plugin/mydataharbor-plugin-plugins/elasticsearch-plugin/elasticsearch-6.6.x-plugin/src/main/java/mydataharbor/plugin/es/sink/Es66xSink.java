package mydataharbor.plugin.es.sink;

import mydataharbor.plugin.es.Es66xClient;
import mydataharbor.sink.AbstractEsSink;
import mydataharbor.sink.EsSinkConfig;
import mydataharbor.sink.es.IEsClient;

/**
 * Created by xulang on 2021/7/27.
 */
public class Es66xSink extends AbstractEsSink {
  public Es66xSink(EsSinkConfig esSinkConfig) {
    super(esSinkConfig);
  }

  @Override
  public IEsClient initEsClient(EsSinkConfig esSinkConfig) {
    return new Es66xClient(esSinkConfig);
  }

  @Override
  public String name() {
    return "es6.6.x 版本的写入器";
  }
}
