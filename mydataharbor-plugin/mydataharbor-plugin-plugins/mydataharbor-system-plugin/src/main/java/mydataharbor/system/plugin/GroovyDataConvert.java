package mydataharbor.system.plugin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.IDataConvertor;
import mydataharbor.IProtocalData;
import mydataharbor.exception.ResetException;
import mydataharbor.setting.BaseSettingContext;
import mydataharbor.system.plugin.exception.ScriptException;
import org.apache.commons.lang3.StringUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * Created by xulang on 2021/9/15.
 */
@Slf4j
public class GroovyDataConvert implements IDataConvertor<IProtocalData, Object, BaseSettingContext> {

  private Class rClass;

  private String script;

  private static ScriptEngineManager factory = new ScriptEngineManager();

  private static ThreadLocal<ScriptEngine> scriptEngineThreadLocal = new ThreadLocal<>();

  private static ScriptEngine getScriptEngine(String script) {
    ScriptEngine scriptEngine = scriptEngineThreadLocal.get();
    if (scriptEngine == null) {
      scriptEngine = factory.getEngineByName("groovy");
      scriptEngineThreadLocal.set(scriptEngine);
      try {
        scriptEngine.eval(script);
      } catch (javax.script.ScriptException e) {
        throw new ScriptException("初始化groovy脚本发生异常:" + e.getMessage(), e);
      }
    }
    return scriptEngine;
  }

  public GroovyDataConvert(String script) {
    this.script = script;
  }

  public void initClass(Class rClass) {
    this.rClass = rClass;
  }

  @Override
  public Object convert(IProtocalData record, BaseSettingContext settingContext) throws ResetException {
    if (StringUtils.isBlank(script)) {
      throw new ScriptException("脚本没有设置！");
    }
    JSONObject input = (JSONObject) JSON.toJSON(record);
    JSONObject output = new JSONObject();
    ScriptEngine scriptEngine = getScriptEngine(script);
    try {
      Invocable inv = (Invocable) scriptEngine;
      inv.invokeFunction("dataConvert", input, output);
    } catch (javax.script.ScriptException | NoSuchMethodException e) {
      throw new ScriptException("执行groovy脚本发生异常:" + e.getMessage(), e);
    }
    return output.toJavaObject(rClass);
  }

  public Class getrClass() {
    return rClass;
  }
}
