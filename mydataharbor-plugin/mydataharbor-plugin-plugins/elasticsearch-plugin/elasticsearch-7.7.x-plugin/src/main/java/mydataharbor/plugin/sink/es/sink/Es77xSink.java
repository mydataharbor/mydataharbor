package mydataharbor.plugin.sink.es.sink;


import mydataharbor.plugin.sink.es.Es77xClient;
import mydataharbor.sink.AbstractEsSink;
import mydataharbor.sink.EsSinkConfig;
import mydataharbor.sink.es.IEsClient;

/**
 * Created by xulang on 2021/7/27.
 */
public class Es77xSink extends AbstractEsSink {
  public Es77xSink(EsSinkConfig esSinkConfig) {
    super(esSinkConfig);
  }

  @Override
  public IEsClient initEsClient(EsSinkConfig esSinkConfig) {
    return new Es77xClient(esSinkConfig);
  }

  @Override
  public String name() {
    return "es7.7.x 版本的写入器";
  }
}
