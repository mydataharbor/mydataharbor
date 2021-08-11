package mydataharbor;

import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Type;

/**
 * 数据转换器
 *
 * @auth xulang
 * @Date 2021/4/29
 **/
public interface IDataConvertor<P extends IProtocalData, R, S extends BaseSettingContext> extends IData {
  /**
   * 由协议数据转成可被执行的writer数据
   *
   * @return
   */
  R convert(P record, S settingContext) throws ResetException;

  default Type getPType() {
    return getTypeByIndex(0, "P", IDataConvertor.class);
  }

  default Type getRType() {
    return getTypeByIndex(1, "R", IDataConvertor.class);
  }

  default Type getSType() {
    return getTypeByIndex(2, "S", IDataConvertor.class);
  }
}
