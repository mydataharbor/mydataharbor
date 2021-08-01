package mydataharbor.plugin.kafka2redis.checker;

import mydataharbor.AbstractDataChecker;
import mydataharbor.plugin.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.setting.BaseSettingContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @auth xulang
 * @Date 2021/5/8
 **/
public class KeyNotEmptyChecker extends AbstractDataChecker<StringKeyValueKafkaRedisProtocal, BaseSettingContext> {

  public KeyNotEmptyChecker() {

  }

  public KeyNotEmptyChecker(AbstractDataChecker next) {
    super(next);
  }

  @Override
  protected CheckResult doCheck(StringKeyValueKafkaRedisProtocal protocalData, BaseSettingContext settingContext) {
    if (StringUtils.isBlank(protocalData.getKey())) {
      return CheckResult.builder().pass(false).msg("key不能为空").build();
    }
    return CheckResult.builder().pass(true).msg("ok").build();
  }
}
