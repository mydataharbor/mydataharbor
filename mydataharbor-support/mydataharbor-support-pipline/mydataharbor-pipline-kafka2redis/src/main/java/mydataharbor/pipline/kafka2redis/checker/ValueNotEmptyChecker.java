package mydataharbor.pipline.kafka2redis.checker;

import mydataharbor.pipline.kafka2redis.protocal.StringKeyValueKafkaRedisProtocal;
import mydataharbor.AbstractDataChecker;
import mydataharbor.setting.BaseSettingContext;
import org.apache.commons.lang3.StringUtils;

/**
 * @auth xulang
 * @Date 2021/5/8
 **/
public class ValueNotEmptyChecker extends AbstractDataChecker<StringKeyValueKafkaRedisProtocal, BaseSettingContext> {

  public ValueNotEmptyChecker() {
  }

  public ValueNotEmptyChecker(AbstractDataChecker next) {
    super(next);
  }

  @Override
  protected CheckResult doCheck(StringKeyValueKafkaRedisProtocal protocalData, BaseSettingContext settingContext) {
    if (StringUtils.isBlank(protocalData.getValue())) {
      return CheckResult.builder().pass(false).msg("value不能为空").build();
    }
    return CheckResult.builder().pass(true).msg("ok").build();
  }

}
