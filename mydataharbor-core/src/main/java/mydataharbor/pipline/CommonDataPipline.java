package mydataharbor.pipline;

import mydataharbor.*;
import mydataharbor.setting.BaseSettingContext;
import lombok.Builder;
import lombok.NonNull;

/**
 * @auth xulang
 * @Date 2021/5/12
 **/
public class CommonDataPipline extends AbstractDataPipline {
  @Builder
  public CommonDataPipline(@NonNull IDataSource dataSource, @NonNull IProtocalDataConvertor protocalDataConvertor, AbstractDataChecker checker, @NonNull IDataConvertor dataConvertor, @NonNull IDataSink sink, BaseSettingContext settingContext) {
    super(dataSource, protocalDataConvertor, checker, dataConvertor, sink, settingContext);
  }
}
