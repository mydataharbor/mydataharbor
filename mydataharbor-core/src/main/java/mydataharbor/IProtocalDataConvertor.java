package mydataharbor;

import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Type;

/**
 * @author xulang
 * 协议转化器,将dataprovider提供的原始介质数据，转化为可处理的协议
 */
public interface IProtocalDataConvertor<T, P extends IProtocalData, S extends BaseSettingContext> extends IData {
  /**
   * 转化，从原始信息，转成可被处理的协议数据
   *
   * @param record
   * @return
   */
  P convert(T record, S settingContext) throws ResetException;

  default Type getTType() {
    return getTypeByIndex(0, "T", IProtocalDataConvertor.class);
  }

  default Type getPType() {
    return getTypeByIndex(1, "P", IProtocalDataConvertor.class);
  }

  default Type getSType() {
    return getTypeByIndex(2, "S", IProtocalDataConvertor.class);
  }

}
