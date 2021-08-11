package mydataharbor.plugin.kafka2redis.convert;

import mydataharbor.plugin.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.IProtocalDataConvertor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/
@Slf4j
public class ProtocalDataConvertor implements IProtocalDataConvertor<ConsumerRecord<String, String>, StringKeyValueKafkaRedisProtocal, BaseSettingContext> {

  @Override
  public StringKeyValueKafkaRedisProtocal convert(ConsumerRecord<String, String> record, BaseSettingContext settingContext) throws ResetException {
    StringKeyValueKafkaRedisProtocal stringKeyValueKafkaRedisProtocal = StringKeyValueKafkaRedisProtocal.builder()
      .key(record.key())
      .opt(StringKeyValueKafkaRedisProtocal.OPT.UPDATE_OR_INSERT)
      .value(record.value())
      .build();
    return stringKeyValueKafkaRedisProtocal;
  }
}
