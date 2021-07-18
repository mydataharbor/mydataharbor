package mydataharbor.pipline.kafka2redis.convert;

import mydataharbor.pipline.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.sink.redis.entity.StringKeyValue;
import mydataharbor.IDataConvertor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

/**
 * @auth xulang
 * @Date 2021/5/6
 **/

public class DataConvertor implements IDataConvertor<StringKeyValueKafkaRedisProtocal, StringKeyValue, BaseSettingContext> {
  @Override
  public StringKeyValue convent(StringKeyValueKafkaRedisProtocal record, BaseSettingContext settingContext) throws ResetException {
    return StringKeyValue.builder().key(record.getKey()).value(record.getValue()).build();
  }
}
