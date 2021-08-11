package mydataharbor.source.kafka.config;

import mydataharbor.datasource.RateLimitConfig;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import lombok.*;

import java.util.List;
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

  public SimpleKafkaConfig(){
    super();

  }


  @MyDataHarborMarker(title = "监听的topic")
  private List<String> topics;

  @MyDataHarborMarker(title = "kafka配置")
  private Properties kafkaConfig;


}
