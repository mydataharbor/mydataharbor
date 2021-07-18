package mydataharbor.pipline.kafka2redis.creator;

import mydataharbor.classutil.classresolver.FieldMarker;
import lombok.Data;
import mydataharbor.*;
import mydataharbor.pipline.kafka2redis.checker.KeyNotEmptyChecker;
import mydataharbor.pipline.kafka2redis.checker.ValueNotEmptyChecker;
import mydataharbor.pipline.kafka2redis.convert.DataConvertor;
import mydataharbor.pipline.kafka2redis.convert.DataProtocalConvertor;
import mydataharbor.pipline.kafka2redis.pipline.Kafka2RedisPipline;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.redis.SingleStringKeyValueRedisSink;
import mydataharbor.sink.redis.config.SingleRedisConfig;
import mydataharbor.source.kafka.SimpleKafkaDataSource;
import mydataharbor.source.kafka.config.SimpleKafkaConfig;

/**
 * @auth xulang
 * @Date 2021/6/23
 **/
public abstract class Kafka2RedisCreator implements IDataSinkCreator<Kafka2RedisCreator.Kafka2RedisCreatorConfig, BaseSettingContext> {

  @Override
  public String type() {
    return "kafka到redis pipline创建器";
  }

  @Override
  public IDataPipline createPipline(Kafka2RedisCreatorConfig config, BaseSettingContext settingContext) throws Exception {
    IDataSource dataSource = new SimpleKafkaDataSource(config.simpleKafkaConfig);
    SingleStringKeyValueRedisSink redisSink = new SingleStringKeyValueRedisSink(config.redisConfig);
    ValueNotEmptyChecker valueChecker = new ValueNotEmptyChecker(null);
    KeyNotEmptyChecker keyChecker = new KeyNotEmptyChecker(valueChecker);
    IDataProtocalConvertor dataProtocalConventor = new DataProtocalConvertor();
    IDataConvertor dataConventor = new DataConvertor();
    Kafka2RedisPipline kafkaRedisPipline =
      Kafka2RedisPipline.builder()
        .dataSource(dataSource)
        .dataProtocalConventor(dataProtocalConventor)
        .checker(keyChecker)
        .dataConventor(dataConventor)
        .sink(redisSink)
        .settingContext(settingContext)
        .build();
    return kafkaRedisPipline;
  }

  @Data
  public static class Kafka2RedisCreatorConfig {

    @FieldMarker(value = "kafka连接参数")
    private SimpleKafkaConfig simpleKafkaConfig;

    @FieldMarker(value = "redis连接参数")
    private SingleRedisConfig redisConfig;
  }
}
