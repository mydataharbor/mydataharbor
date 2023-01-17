package mydataharbor.system.plugin;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.IProtocolData;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.system.plugin.exception.ScriptException;

import java.lang.reflect.Method;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

/**
 * 动态加载 groovy class
 * Created by xulang on 2021/9/15.
 */
@Slf4j
public class GroovyClassDataConverter extends GroovyDataConverter {

  private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

  private Object obj;

  private Method method;

  public GroovyClassDataConverter(String script) {
    super(script);

    try {
      Class clazz = groovyClassLoader.parseClass(script);
      if (clazz == null) {
        throw new ScriptException("加载groovy class失败！");
      }
      this.obj = clazz.newInstance();
      this.method = clazz.getMethod("dataConvert", Object.class, Object.class);
    } catch (Exception e) {
      throw new ScriptException("加载groovy class失败：" + e.getMessage(), e);
    }

  }

  @Override
  public Object convert(IProtocolData record, BaseSettingContext settingContext) throws ResetException {
    JSONObject input = (JSONObject) JSON.toJSON(record);
    JSONObject output = new JSONObject();

    try {
      method.invoke(obj, input, output);
    } catch (Exception e) {
      throw new ScriptException("执行groovy脚本发生异常:" + e.getMessage(), e);
    }
    return output.toJavaObject(getrClass());
  }
}
