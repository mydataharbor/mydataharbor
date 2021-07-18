package mydataharbor.pipline.kafka2redis.convert;

import mydataharbor.pipline.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.IDataProtocalConvertor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/
@Slf4j
public class DataProtocalConvertor implements IDataProtocalConvertor<ConsumerRecord<String, String>, StringKeyValueKafkaRedisProtocal, BaseSettingContext> {

  @Override
  public StringKeyValueKafkaRedisProtocal convent(ConsumerRecord<String, String> record, BaseSettingContext settingContext) throws ResetException {
    StringKeyValueKafkaRedisProtocal stringKeyValueKafkaRedisProtocal = StringKeyValueKafkaRedisProtocal.builder()
      .key(record.key())
      .opt(StringKeyValueKafkaRedisProtocal.OPT.UPDATE_OR_INSERT)
      .value(record.value())
      .build();
    return stringKeyValueKafkaRedisProtocal;
  }
}
