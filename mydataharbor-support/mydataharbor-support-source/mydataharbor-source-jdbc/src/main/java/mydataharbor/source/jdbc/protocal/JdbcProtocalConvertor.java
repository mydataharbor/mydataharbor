package mydataharbor.source.jdbc.protocal;

import mydataharbor.IProtocalDataConvertor;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.source.jdbc.JdbcResult;

/**
 * Created by xulang on 2021/8/22.
 */
@MyDataHarborMarker(title = "jdbc数据源默认协议数据转换器")
public class JdbcProtocalConvertor implements IProtocalDataConvertor<JdbcResult, JdbcProtocalData, BaseSettingContext> {
  @Override
  public JdbcProtocalData convert(JdbcResult record, BaseSettingContext settingContext) throws ResetException {
    return new JdbcProtocalData(record);
  }
}
