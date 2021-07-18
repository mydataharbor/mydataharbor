package mydataharbor.pipline.kafka2redis.config;

import mydataharbor.sink.redis.config.SingleRedisConfig;
import mydataharbor.source.kafka.config.SimpleKafkaConfig;
import mydataharbor.setting.BaseSettingContext;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/
@Data
@SuperBuilder
public class Kafka2RedisPiplineSettingContext extends BaseSettingContext {

  /**
   * kafka配置
   */
  private SimpleKafkaConfig simpleKafkaConfig;

  /**
   * redis配置
   */
  private SingleRedisConfig singleRedisConfig;


}
