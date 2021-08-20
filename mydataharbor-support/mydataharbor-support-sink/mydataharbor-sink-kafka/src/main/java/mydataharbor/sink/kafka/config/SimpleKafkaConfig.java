package mydataharbor.sink.kafka.config;

import lombok.Data;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.datasource.RateLimitConfig;

import java.util.Properties;

/**
 * kafka配置
 *
 * @auth xulang
 * @Date 2021/5/7
 **/
@MyDataHarborMarker(title = "普通kafka服务连接配置")
@Data
public class SimpleKafkaConfig extends RateLimitConfig {

  public SimpleKafkaConfig() {
    super();
  }

  @MyDataHarborMarker(title = "要写入的topic")
  private String topic;

  @MyDataHarborMarker(title = "kafka配置")
  private Properties kafkaConfig;

  @MyDataHarborMarker(title = "是否同步发送")
  private boolean sync = false;
}
