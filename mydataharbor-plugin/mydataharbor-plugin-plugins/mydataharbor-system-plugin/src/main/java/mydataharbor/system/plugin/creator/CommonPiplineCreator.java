package mydataharbor.system.plugin.creator;

import mydataharbor.creator.AbstractCommonDataPiplineCreator;
import mydataharbor.creator.ConstructorAndArgs;
import mydataharbor.exception.InstanceCreateException;
import mydataharbor.plugin.base.util.JsonUtil;
import mydataharbor.system.plugin.SystemPlugin;
import mydataharbor.classutil.classresolver.FieldMarker;
import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @auth xulang
 * @Date 2021/6/22
 **/
@FieldMarker(value = "通用构建器")
@Extension
public class CommonPiplineCreator extends AbstractCommonDataPiplineCreator implements ExtensionPoint {

  private Reflections reflections;

  public CommonPiplineCreator() {
    this.reflections = new Reflections("mydataharbor");
  }

  @Override
  public <T> T createInstanc(ConstructorAndArgs constructorAndArgs, Class<T> targetClazz) throws InstanceCreateException {
    try {
      ClassLoader pluginClassLoader = SystemPlugin.getPluginManager().getPluginClassLoader(constructorAndArgs.getPluginId());
      if(pluginClassLoader==null){
        throw new InstanceCreateException("目标分组，没有安装该插件:"+constructorAndArgs.getPluginId());
      }
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
