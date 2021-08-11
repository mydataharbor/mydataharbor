package mydataharbor.creator;

import mydataharbor.*;
import mydataharbor.exception.InstanceCreateException;
import mydataharbor.pipline.CommonDataPipline;
import mydataharbor.setting.BaseSettingContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 抽象通用pipline创建器
 * 实现自动扫描本包下的可用资源
 *
 * @auth xulang
 * @Date 2021/5/25
 **/
public abstract class AbstractCommonDataPiplineCreator implements IDataSinkCreator<CommonPiplineCreatorConfig, BaseSettingContext> {

  @Override
  public IDataPipline createPipline(CommonPiplineCreatorConfig config, BaseSettingContext settingContext) throws Exception {
    IDataSource dataSource = createInstanc(config.getDataSourceConstructorAndArgs(), IDataSource.class);
    IProtocalDataConvertor protocalDataConvertor = createInstanc(config.getProtocalDataConvertorConstructorAndArgs(), IProtocalDataConvertor.class);
    IDataConvertor dataConvertor = createInstanc(config.getDataConvertorConstructorAndArgs(), IDataConvertor.class);
    IDataSink dataSink = createInstanc(config.getDataSinkConstructorAndArgs(), IDataSink.class);
    AbstractDataChecker checker = null;
    if (config.getDataCheckerConstructorAndArgs() != null) {
      //创建责任链模式的checker
      for (int i = config.getDataCheckerConstructorAndArgs().size() - 1; i >= 0; i--) {
        AbstractDataChecker tmpChecker = createInstanc(config.getDataCheckerConstructorAndArgs().get(i), AbstractDataChecker.class);
        tmpChecker.setNext(checker);
        checker = tmpChecker;
      }
    }
    if (settingContext == null) {
      Class<? extends BaseSettingContext> settingContextClazz = (Class<? extends BaseSettingContext>) Class.forName(config.getSettingContextClazz());
      settingContext = parseJson(config.getSettingContextJsonValue(), settingContextClazz);
    }
    return CommonDataPipline.builder()
      .dataSource(dataSource)
      .protocalDataConvertor(protocalDataConvertor)
      .checker(checker)
      .dataConvertor(dataConvertor)
      .sink(dataSink)
      .settingContext(settingContext)
      .build();
  }


  public <T> T createInstanc(ConstructorAndArgs constructorAndArgs, Class<T> targetClazz) throws InstanceCreateException {
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


}
