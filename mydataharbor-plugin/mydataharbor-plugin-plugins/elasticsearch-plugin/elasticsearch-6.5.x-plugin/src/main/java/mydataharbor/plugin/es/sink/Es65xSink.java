package mydataharbor.plugin.es.sink;

import mydataharbor.plugin.es.Es65xClient;
import mydataharbor.sink.AbstractEsSink;
import mydataharbor.sink.EsSinkConfig;
import mydataharbor.sink.es.IEsClient;

/**
 * Created by xulang on 2021/7/27.
 */
public class Es65xSink extends AbstractEsSink {
  public Es65xSink(EsSinkConfig esSinkConfig) {
    super(esSinkConfig);
  }

  @Override
  public IEsClient initEsClient(EsSinkConfig esSinkConfig) {
    return new Es65xClient(esSinkConfig);
  }

  @Override
  public String name() {
    return "es6.5.x 版本的写入器";
  }
}
