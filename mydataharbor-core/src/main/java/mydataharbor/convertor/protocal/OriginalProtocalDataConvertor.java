package mydataharbor.convertor.protocal;

import mydataharbor.IProtocalDataConvertor;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

/**
 * 原样输出转换器
 * Created by xulang on 2021/8/10.
 */
public class OriginalProtocalDataConvertor<T, S extends BaseSettingContext> implements IProtocalDataConvertor<T, OriginalProtocalData, S> {

  @Override
  public OriginalProtocalData convert(T record, S settingContext) throws ResetException {
    return new OriginalProtocalData(record);
  }
}
