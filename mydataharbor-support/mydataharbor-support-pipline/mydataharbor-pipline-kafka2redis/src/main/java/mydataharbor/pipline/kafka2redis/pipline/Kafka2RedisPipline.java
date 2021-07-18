package mydataharbor.pipline.kafka2redis.pipline;


import mydataharbor.pipline.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.sink.redis.entity.StringKeyValue;
import mydataharbor.*;
import mydataharbor.pipline.AbstractDataPipline;
import mydataharbor.setting.BaseSettingContext;
import lombok.Builder;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @auth xulang
 * @Date 2021/5/7
 **/
public class Kafka2RedisPipline extends AbstractDataPipline<ConsumerRecord<String, String>, StringKeyValueKafkaRedisProtocal, StringKeyValue, BaseSettingContext> {

  @Builder
  public Kafka2RedisPipline(
    IDataSource<ConsumerRecord<String, String>, BaseSettingContext> dataSource,
    IDataProtocalConvertor<ConsumerRecord<String, String>, StringKeyValueKafkaRedisProtocal, BaseSettingContext> dataProtocalConventor,
    AbstractDataChecker checker,
    IDataConvertor<StringKeyValueKafkaRedisProtocal, StringKeyValue, BaseSettingContext> dataConventor,
    IDataSink<StringKeyValue, BaseSettingContext> sink, BaseSettingContext settingContext) {
    super(dataSource, dataProtocalConventor, checker, dataConventor, sink, settingContext);
  }
}
