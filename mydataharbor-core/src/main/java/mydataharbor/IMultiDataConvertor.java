package mydataharbor;

import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Type;
import java.util.List;

/**
 * 1对多
 * 数据转换器
 *
 * @auth xulang
 * @Date 2021/4/29
 **/
public interface IMultiDataConvertor<P extends IProtocalData, R, S extends BaseSettingContext> extends IDataConvertor<P, List<R>, S> {
  @Override
  default Type getRType() {
    return getTypeByIndex(1, "R", IMultiDataConvertor.class);
  }
}
