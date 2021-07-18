package mydataharbor;

import mydataharbor.setting.BaseSettingContext;

/**
 * 数据检查器，责任链模式
 */
public abstract class AbstractDataChecker<P extends IProtocalData, S extends BaseSettingContext> implements IProtocalDataChecker<P, S> {

  private AbstractDataChecker<P, S> next;

  public AbstractDataChecker() {

  }

  public AbstractDataChecker(AbstractDataChecker<P, S> next) {
    this.next = next;
  }

  public void setNext(AbstractDataChecker<P, S> next) {
    this.next = next;
  }

  /**
   * 返回最后一个check结果，遇到检查不通过的就地返回
   *
   * @return
   */
  public CheckResult check(CheckResult preCheckResult, P protocalData, S settingContext) {
    CheckResult checkResult = doCheck(protocalData, settingContext);
    checkResult.setPre(preCheckResult);
    if (checkResult.isPass() && next != null) {
      return next.check(checkResult, protocalData, settingContext);
    } else {
      return checkResult;
    }
  }

  protected abstract CheckResult doCheck(P protocalData, S settingContext);


}