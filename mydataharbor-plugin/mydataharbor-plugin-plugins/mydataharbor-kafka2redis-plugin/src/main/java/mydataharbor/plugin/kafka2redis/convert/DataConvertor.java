package mydataharbor.plugin.kafka2redis.convert;

import mydataharbor.plugin.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.IDataConvertor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.sink.redis.entity.StringKeyValue;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/

public class DataConvertor implements IDataConvertor<StringKeyValueKafkaRedisProtocal, StringKeyValue, BaseSettingContext> {
  @Override
  public StringKeyValue convert(StringKeyValueKafkaRedisProtocal record, BaseSettingContext settingContext) throws ResetException {
    return StringKeyValue.builder().key(record.getKey()).value(record.getValue()).build();
  }
}
