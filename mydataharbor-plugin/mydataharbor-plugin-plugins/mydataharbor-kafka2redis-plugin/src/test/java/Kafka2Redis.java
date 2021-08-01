import mydataharbor.IDataConvertor;
import mydataharbor.IDataProtocalConvertor;
import mydataharbor.executor.CommonDataExecutor;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.plugin.kafka2redis.checker.KeyNotEmptyChecker;
import mydataharbor.plugin.kafka2redis.checker.ValueNotEmptyChecker;
import mydataharbor.plugin.kafka2redis.convert.DataConvertor;
import mydataharbor.plugin.kafka2redis.convert.DataProtocalConvertor;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.redis.SingleStringKeyValueRedisSink;
import mydataharbor.sink.redis.config.SingleRedisConfig;
import mydataharbor.source.kafka.SimpleKafkaDataSource;
import mydataharbor.source.kafka.config.SimpleKafkaConfig;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/

public class Kafka2Redis {

  @Test
  public void test() throws InterruptedException {
    BaseSettingContext baseSettingContext = BaseSettingContext.builder()
      .parallel(false)
      .threadNum(10)
      .batchWrite(false)
      .sleepTime(1000)
      .build();
    Properties kafkaProperties = new Properties();
    kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    kafkaProperties.put("enable.auto.commit", false);
    kafkaProperties.put("auto.offset.reset", "earliest");
    kafkaProperties.put("bootstrap.servers", "127.0.0.1:9092");
    kafkaProperties.put("group.id", "test2");
    SimpleKafkaConfig simpleKafkaConfig = new SimpleKafkaConfig();
    simpleKafkaConfig.setRateGroup("test");
    simpleKafkaConfig.setSpeed(100L);
    simpleKafkaConfig.setKafkaConfig(kafkaProperties);
    simpleKafkaConfig.setTopics(Arrays.asList("test2"));
    SimpleKafkaDataSource simpleKafkaDataSource = new SimpleKafkaDataSource(simpleKafkaConfig);
    SingleRedisConfig singleRedisConfig = SingleRedisConfig.builder().host("127.0.0.1").port(6379).timeout(1000).build();
    SingleStringKeyValueRedisSink redisSink = new SingleStringKeyValueRedisSink(singleRedisConfig);
    ValueNotEmptyChecker valueChecker = new ValueNotEmptyChecker(null);
    KeyNotEmptyChecker keyChecker = new KeyNotEmptyChecker(valueChecker);

    IDataProtocalConvertor dataProtocalConventor = new DataProtocalConvertor();
    IDataConvertor dataConventor = new DataConvertor();

    CommonDataPipline kafkaRedisPipline =
      CommonDataPipline.builder()
        .dataSource(simpleKafkaDataSource)
        .dataProtocalConventor(dataProtocalConventor)
        .checker(keyChecker)
        .dataConventor(dataConventor)
        .sink(redisSink)
        .settingContext(baseSettingContext)
        .build();


    CommonDataExecutor commonDataExecutor = new CommonDataExecutor(kafkaRedisPipline, "test-1");
    commonDataExecutor.start();
    commonDataExecutor.join();
  }

}