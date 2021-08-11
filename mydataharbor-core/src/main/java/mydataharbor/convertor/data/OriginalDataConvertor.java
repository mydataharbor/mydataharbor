package mydataharbor.convertor.data;

import mydataharbor.IDataConvertor;
import mydataharbor.convertor.protocal.OriginalProtocalData;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

/**
 * Created by xulang on 2021/8/10.
 */
public class OriginalDataConvertor<T, S extends BaseSettingContext> implements IDataConvertor<OriginalProtocalData<T>, T, S> {

  @Override
  public T convert(OriginalProtocalData<T> record, S settingContext) throws ResetException {
    return record.getRecord();
  }
}
