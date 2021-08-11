package mydataharbor.plugin.base.creator;

import mydataharbor.*;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.setting.BaseSettingContext;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 自动扫描功能的creator
 * @auth xulang
 * @Date 2021/6/22
 **/
public abstract class AbstractAutoScanPiplineCreator<C, S extends BaseSettingContext>  implements  IDataSinkCreator<C, S> {

  private Reflections reflections;

  public AbstractAutoScanPiplineCreator() {
    this.reflections = new Reflections(scanPackage());
  }

  @Override
  public Set<Class<? extends IDataSource>> availableDataSource() {
    return getSubTypesOf(IDataSource.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends IProtocalDataConvertor>> availableProtocalDataConvertor() {
    return getSubTypesOf(IProtocalDataConvertor.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends AbstractDataChecker>> avaliableDataChecker() {
    return getSubTypesOf(AbstractDataChecker.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends IDataConvertor>> avaliabledataConvertor() {
    return getSubTypesOf(IDataConvertor.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends IDataSink>> avaliableDataSink() {
    return getSubTypesOf(IDataSink.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends BaseSettingContext>> avaliableSettingContext() {
    return getSubTypesOf(BaseSettingContext.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  public abstract String scanPackage();

  @Override
  public <T> T parseJson(String json, Class<T> clazz) {
    return JsonUtil.jsonToObject(json, clazz);
  }

  public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz) {
    return reflections.getSubTypesOf(clazz);
  }

}
