package mydataharbor.plugin.kafka2redis;

import lombok.Data;
import mydataharbor.*;
import mydataharbor.classutil.classresolver.FieldMarker;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.plugin.kafka2redis.checker.KeyNotEmptyChecker;
import mydataharbor.plugin.kafka2redis.checker.ValueNotEmptyChecker;
import mydataharbor.plugin.kafka2redis.convert.DataConvertor;
import mydataharbor.plugin.kafka2redis.convert.DataProtocalConvertor;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.redis.SingleStringKeyValueRedisSink;
import mydataharbor.sink.redis.config.SingleRedisConfig;
import mydataharbor.source.kafka.SimpleKafkaDataSource;
import mydataharbor.source.kafka.config.SimpleKafkaConfig;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

/**
 * @auth xulang
 * @Date 2021/5/16
 **/
@Extension
@FieldMarker(value = "kafka到redis创建器")
public class Kafka2RedisCreator implements IDataSinkCreator<Kafka2RedisCreator.Kafka2RedisCreatorConfig, BaseSettingContext>, ExtensionPoint {

  public Kafka2RedisCreator() {

  }

  @Override
  public <T> T parseJson(String json, Class<T> clazz) {
    return JsonUtil.jsonToObject(json, clazz);
  }

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
    CommonDataPipline kafkaRedisPipline =
      CommonDataPipline.builder()
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
