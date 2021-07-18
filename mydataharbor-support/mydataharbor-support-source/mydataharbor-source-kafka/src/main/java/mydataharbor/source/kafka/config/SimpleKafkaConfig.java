package mydataharbor.source.kafka.config;

import mydataharbor.datasource.RateLimitConfig;
import mydataharbor.classutil.classresolver.FieldMarker;
import lombok.*;

import java.util.List;
import java.util.Properties;

/**
 * kafka配置
 *
 * @auth xulang
 * @Date 2021/5/7
 **/
@FieldMarker(value = "普通kafka服务连接配置")
@Data
public class SimpleKafkaConfig extends RateLimitConfig {

  public SimpleKafkaConfig(){
    super();

  }


  @FieldMarker(value = "监听的topic")
  private List<String> topics;

  @FieldMarker(value = "kafka配置")
  private Properties kafkaConfig;


}
