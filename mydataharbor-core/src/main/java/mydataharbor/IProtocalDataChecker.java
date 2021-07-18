package mydataharbor;

import mydataharbor.setting.BaseSettingContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.lang.reflect.Type;

/**
 * @auth xulang
 * @Date 2021/5/10
 **/
public interface IProtocalDataChecker<P extends IProtocalData, S extends BaseSettingContext> extends IData {

  CheckResult check(CheckResult preCheckResult, P protocalData, S settingContext);

  default Type getPType() {
    return getTypeByIndex(0, "P", IProtocalDataChecker.class);
  }

  default Type getSType() {
    return getTypeByIndex(1, "S", IProtocalDataChecker.class);
  }

  /**
   * 检查结果
   */
  @Data
  @SuperBuilder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CheckResult {

    /**
     * 上级check结果，一般是通过的信息
     */
    private CheckResult pre;

    /**
     * 状态，true，通过，false不通过
     */
    private boolean pass;
    /**
     * 检查详情
     */
    private String msg;
  }
}
