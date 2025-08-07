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


package mydataharbor.pipeline.creator;

import mydataharbor.AbstractDataChecker;
import mydataharbor.IDataConverter;
import mydataharbor.IDataPipeline;
import mydataharbor.IDataPipelineCreator;
import mydataharbor.IDataSink;
import mydataharbor.IDataSource;
import mydataharbor.IProtocolDataConverter;
import mydataharbor.exception.InstanceCreateException;
import mydataharbor.pipeline.CommonDataPipeline;
import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 抽象通用pipeline创建器
 * 实现自动扫描本包下的可用资源
 *
 * @auth xulang
 * @Date 2021/5/25
 **/
public abstract class AbstractCommonDataPipelineCreator implements IDataPipelineCreator<CommonPipelineCreatorConfig, BaseSettingContext> {


  @Override
  public IDataPipeline createPipeline(CommonPipelineCreatorConfig config, BaseSettingContext settingContext) throws Exception {
    IDataSource dataSource = createInstance(config.getDataSource(), IDataSource.class);
    IProtocolDataConverter protocolDataConverter = createInstance(config.getProtocolDataConverter(), IProtocolDataConverter.class);
    IDataConverter dataConverter = createInstance(config.getDataConverter(), IDataConverter.class);
    IDataSink dataSink = createInstance(config.getDataSink(), IDataSink.class);
    AbstractDataChecker checker = null;
    if (config.getDataCheckers() != null) {
      //创建责任链模式的checker
      for (int i = config.getDataCheckers().size() - 1; i >= 0; i--) {
        AbstractDataChecker tmpChecker = createInstance(config.getDataCheckers().get(i), AbstractDataChecker.class);
        tmpChecker.setNext(checker);
        checker = tmpChecker;
      }
    }
    if (settingContext == null) {
      Class<? extends BaseSettingContext> settingContextClazz = (Class<? extends BaseSettingContext>) Class.forName(config.getSettingContextClazz());
      settingContext = parseJson(config.getSettingContextJsonValue(), settingContextClazz);
    }
    return buildPipeline(settingContext, dataSource, protocolDataConverter, dataConverter, dataSink, checker);
  }

  public IDataPipeline buildPipeline(BaseSettingContext settingContext, IDataSource dataSource, IProtocolDataConverter protocolDataConverter, IDataConverter dataConverter, IDataSink dataSink, AbstractDataChecker checker) {
    return CommonDataPipeline.builder()
      .dataSource(dataSource)
      .protocolDataConverter(protocolDataConverter)
      .checker(checker)
      .dataConverter(dataConverter)
      .sink(dataSink)
      .settingContext(settingContext)
      .build();
  }


  public <T> T createInstance(ConstructorAndArgs constructorAndArgs, Class<T> targetClazz) throws InstanceCreateException {
    try {
      Class<? extends T> clazz = (Class<? extends T>) Class.forName(constructorAndArgs.getClazz(), true, getClass().getClassLoader());
      List<String> argsTypeStrList = constructorAndArgs.getArgsType();
      List<Class<?>> argsType = new ArrayList<>();
      List<Object> argsValue = new ArrayList<>();
      for (int i = 0; i < argsTypeStrList.size(); i++) {
        String s = argsTypeStrList.get(i);
        Class<?> aClass = Class.forName(s, true, getClass().getClassLoader());
        argsType.add(aClass);
        argsValue.add(parseJson(constructorAndArgs.getArgsJsonValue().get(i), aClass));
      }
      Constructor<? extends T> constructor = clazz.getConstructor(argsType.toArray(new Class[argsType.size()]));
      return constructor.newInstance(argsValue.toArray());
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new InstanceCreateException("创建实例失败！", e);
    }
  }

  public abstract <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz);

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


}