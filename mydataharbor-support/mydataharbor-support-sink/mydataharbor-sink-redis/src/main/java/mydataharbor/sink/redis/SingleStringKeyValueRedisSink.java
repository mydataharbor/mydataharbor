package mydataharbor.sink.redis;

import mydataharbor.sink.redis.config.SingleRedisConfig;
import mydataharbor.sink.redis.entity.StringKeyValue;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 单机redis 字符串 key/value
 * 写入
 *
 * @auth xulang
 * @Date 2021/4/30
 **/
public class SingleStringKeyValueRedisSink extends AbstractSingleRedisSink<StringKeyValue, BaseSettingContext> {

  public SingleStringKeyValueRedisSink(SingleRedisConfig redisConfig) {
    super(redisConfig);
  }

  @Override
  public WriterResult write(StringKeyValue record, BaseSettingContext settingContext) throws ResetException {
    try {
      String res = getConn().sync().set(record.getKey(), record.getValue());
      return WriterResult.builder().commit(true).msg(res).success(true).build();
    } catch (Exception e) {
      return WriterResult.builder().success(false).commit(false).msg(e.getMessage()).build();
      //包装成resetexception，用于回滚
      // throw new ResetException("单条写入" + record + "失败", e);
    }
  }

  @Override
  public WriterResult write(List<StringKeyValue> records, BaseSettingContext settingContext) throws ResetException {
    try {
      Map<String, String> map = records.parallelStream().collect(Collectors.toMap(StringKeyValue::getKey, StringKeyValue::getValue, (key1, key2) -> key2));
      String mset = getConn().sync().mset(map);
      return WriterResult.builder().commit(true).success(true).msg(mset).build();
    } catch (Exception e) {
      return WriterResult.builder().success(false).commit(false).msg(e.getMessage()).build();
      //包装成resetexception，用于回滚
      // throw new ResetException("批量写入" + records + "失败：" + e.getMessage(), e);
    }
  }
}
