/*
 * 版权所有 (C) [2020] [xulang 1053618636@qq.com]
 *
 * 此程序是自由软件：您可以根据自由软件基金会发布的 GNU 通用公共许可证第3版或
 * （根据您的选择）任何更高版本重新分发和/或修改它。
 *
 * 此程序基于希望它有用而分发，但没有任何担保；甚至没有对适销性或特定用途适用性的隐含担保。详见 GNU 通用公共许可证。
 *
 * 您应该已经收到 GNU 通用公共许可证的副本。如果没有，请参阅
 * <http://www.gnu.org/licenses/>.
 *
 */


package mydataharbor.plugin.base.creator;

import mydataharbor.AbstractDataChecker;
import mydataharbor.IDataConverter;
import mydataharbor.IDataPipelineCreator;
import mydataharbor.IDataSink;
import mydataharbor.IDataSource;
import mydataharbor.IProtocolDataConverter;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

/**
 * 自动扫描功能的creator
 * @auth xulang
 * @Date 2021/6/22
 **/
public abstract class AbstractAutoScanPipelineCreator<C, S extends BaseSettingContext>  implements IDataPipelineCreator<C, S> {

  private Reflections reflections;

  public AbstractAutoScanPipelineCreator() {
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
  public Set<Class<? extends IProtocolDataConverter>> availableProtocolDataConverter() {
    return getSubTypesOf(IProtocolDataConverter.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends AbstractDataChecker>> availableDataChecker() {
    return getSubTypesOf(AbstractDataChecker.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends IDataConverter>> availableDataConverter() {
    return getSubTypesOf(IDataConverter.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends IDataSink>> availableDataSink() {
    return getSubTypesOf(IDataSink.class)
      .stream()
      .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
      .collect(Collectors.toSet());
  }

  @Override
  public Set<Class<? extends BaseSettingContext>> availableSettingContext() {
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