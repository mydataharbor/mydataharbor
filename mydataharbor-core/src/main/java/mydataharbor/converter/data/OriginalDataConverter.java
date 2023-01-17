package mydataharbor.converter.data;

import mydataharbor.IDataConverter;
import mydataharbor.converter.protocol.OriginalProtocolData;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

/**
 * Created by xulang on 2021/8/10.
 */
public class OriginalDataConverter<T, S extends BaseSettingContext> implements IDataConverter<OriginalProtocolData<T>, T, S> {

  @Override
  public T convert(OriginalProtocolData<T> record, S settingContext) throws ResetException {
    return record.getRecord();
  }
}
