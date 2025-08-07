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


package mydataharbor.system.plugin.creator;

import mydataharbor.AbstractDataChecker;
import mydataharbor.IDataConverter;
import mydataharbor.IDataPipeline;
import mydataharbor.IDataSink;
import mydataharbor.IDataSource;
import mydataharbor.IProtocolDataConverter;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.exception.InstanceCreateException;
import mydataharbor.pipeline.creator.AbstractCommonDataPipelineCreator;
import mydataharbor.pipeline.creator.ConstructorAndArgs;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.system.plugin.GroovyDataConverter;
import mydataharbor.system.plugin.SystemPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.reflections.Reflections;

/**
 * @auth xulang
 * @Date 2021/6/22
 **/
@MyDataHarborMarker(title = "通用构建器")
@Extension
public class CommonPipelineCreator extends AbstractCommonDataPipelineCreator implements ExtensionPoint {

  private Reflections reflections;

  public CommonPipelineCreator() {
    this.reflections = new Reflections("mydataharbor");
  }

  @Override
  public IDataPipeline buildPipeline(BaseSettingContext settingContext, IDataSource dataSource, IProtocolDataConverter protocolDataConverter, IDataConverter dataConverter, IDataSink dataSink, AbstractDataChecker checker) {
    if(dataConverter instanceof GroovyDataConverter){
      GroovyDataConverter groovyDataConvert = (GroovyDataConverter) dataConverter;
      groovyDataConvert.initClass((Class) dataSink.getRType());
    }
    return super.buildPipeline(settingContext, dataSource, protocolDataConverter, dataConverter, dataSink, checker);
  }



  @Override
  public <T> T createInstance(ConstructorAndArgs constructorAndArgs, Class<T> targetClazz) throws InstanceCreateException {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader pluginClassLoader = SystemPlugin.getPluginManager().getPluginClassLoader(constructorAndArgs.getPluginId());
      if(pluginClassLoader==null){
        throw new InstanceCreateException("目标分组，没有安装该插件:"+constructorAndArgs.getPluginId());
      }
      Thread.currentThread().setContextClassLoader(pluginClassLoader);
      Class<? extends T> clazz = (Class<? extends T>) Class.forName(constructorAndArgs.getClazz(), true, pluginClassLoader);
      List<String> argsTypeStrList = constructorAndArgs.getArgsType();
      List<Class<?>> argsType = new ArrayList<>();
      List<Object> argsValue = new ArrayList<>();
      for (int i = 0; i < argsTypeStrList.size(); i++) {
        String s = argsTypeStrList.get(i);
        Class<?> aClass = Class.forName(s, true, pluginClassLoader);
        argsType.add(aClass);
        argsValue.add(parseJson(constructorAndArgs.getArgsJsonValue().get(i), aClass));
      }
      Constructor<? extends T> constructor = clazz.getConstructor(argsType.toArray(new Class[argsType.size()]));
      return constructor.newInstance(argsValue.toArray());
    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new InstanceCreateException("创建实例失败！", e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  @Override
  public String type() {
    return "通用构建器";
  }

  @Override
  public <T> T parseJson(String json, Class<T> clazz) {
    return JsonUtil.jsonToObject(json, clazz);
  }


  @Override
  public <T> Set<Class<? extends T>> getSubTypesOf(Class<T> clazz) {
    return reflections.getSubTypesOf(clazz);
  }

}