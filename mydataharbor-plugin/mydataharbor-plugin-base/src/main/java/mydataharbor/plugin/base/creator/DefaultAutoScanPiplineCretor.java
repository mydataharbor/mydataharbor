package mydataharbor.plugin.base.creator;

import mydataharbor.IDataPipline;
import mydataharbor.setting.BaseSettingContext;

import java.util.Map;

/**
 * Created by xulang on 2021/8/17.
 */
public class DefaultAutoScanPiplineCretor extends AbstractAutoScanPiplineCreator<Map<String, Object>, BaseSettingContext> {
  @Override
  public String scanPackage() {
    return null;
  }

  @Override
  public String type() {
    return "通用组件扫描器";
  }

  @Override
  public IDataPipline createPipline(Map<String, Object> config, BaseSettingContext settingContext) throws Exception {
    throw new RuntimeException("改创建器无法创建pipline");
  }

  @Override
  public boolean canCreatePipline() {
    return false;
  }
}
