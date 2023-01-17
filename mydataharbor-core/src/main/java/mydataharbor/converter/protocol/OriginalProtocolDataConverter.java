package mydataharbor.converter.protocol;

import mydataharbor.IProtocolDataConverter;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;

/**
 * 原样输出转换器
 * Created by xulang on 2021/8/10.
 */
@MyDataHarborMarker(title = "原样输出转换器")
public class OriginalProtocolDataConverter<T, S extends BaseSettingContext> implements IProtocolDataConverter<T, OriginalProtocolData, S> {

  @Override
  public OriginalProtocolData convert(T record, S settingContext) throws ResetException {
    return new OriginalProtocolData(record);
  }
}
