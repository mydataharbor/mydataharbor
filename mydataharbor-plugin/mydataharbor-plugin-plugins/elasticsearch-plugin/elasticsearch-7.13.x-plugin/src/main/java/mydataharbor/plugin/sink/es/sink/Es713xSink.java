package mydataharbor.plugin.sink.es.sink;


import mydataharbor.plugin.sink.es.Es713xClient;
import mydataharbor.sink.AbstractEsSink;
import mydataharbor.sink.EsSinkConfig;
import mydataharbor.sink.es.IEsClient;

/**
 * Created by xulang on 2021/7/27.
 */
public class Es713xSink extends AbstractEsSink {
  public Es713xSink(EsSinkConfig esSinkConfig) {
    super(esSinkConfig);
  }

  @Override
  public IEsClient initEsClient(EsSinkConfig esSinkConfig) {
    return new Es713xClient(esSinkConfig);
  }

  @Override
  public String name() {
    return "es7.7.x 版本的写入器";
  }
}
