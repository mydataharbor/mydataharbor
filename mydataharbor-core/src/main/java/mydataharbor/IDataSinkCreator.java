package mydataharbor;

import mydataharbor.setting.BaseSettingContext;

import java.util.Collections;
import java.util.Set;

/**
 * pipline创建器
 *
 * @auth xulang
 * @Date 2021/5/8
 **/
public interface IDataSinkCreator<C, S extends BaseSettingContext> extends IData {

  /**
   * 创建器名称
   *
   * @return
   */
  String type();

  /**
   * 通过配置创建pipline
   *
   * @param config
   * @return
   */
  IDataPipline createPipline(C config, S settingContext) throws Exception;

  /**
   * 是否可以创建pipline，有些creator只提供可用的资源
   *
   * @return
   */
  default boolean canCreatePipline() {
    return true;
  }


  /**
   * 获得config类型
   *
   * @return
   */
  default Class<C> getConfigClass() {
    return getTypeByIndex(0, "C", IDataSinkCreator.class);
  }

  /**
   * json转对象
   *
   * @param json
   * @param clazz
   * @param <T>
   * @return
   */
  <T> T parseJson(String json, Class<T> clazz);

  /**
   * 获得setting类型
   *
   * @return
   */
  default Class<S> getSettingClass() {
    return getTypeByIndex(1, "S", IDataSinkCreator.class);
  }

  /**
   * 可用的数据源
   *
   * @return
   */
  default Set<Class<? extends IDataSource>> availableDataSource() {
    return Collections.emptySet();
  }

  /**
   * 可用的协议转换数据
   *
   * @return
   */
  default Set<Class<? extends IDataProtocalConvertor>> availableDataProtocalConventor() {
    return Collections.emptySet();
  }

  /**
   * 可用的数据检查器
   *
   * @return
   */
  default Set<Class<? extends AbstractDataChecker>> avaliableDataChecker() {
    return Collections.emptySet();
  }


  /**
   * 可用的数据转换器
   *
   * @return
   */
  default Set<Class<? extends IDataConvertor>> avaliableDataConventor() {
    return Collections.emptySet();
  }


  /**
   * 可用的数据写入器
   *
   * @return
   */
  default Set<Class<? extends IDataSink>> avaliableDataSink() {
    return Collections.emptySet();
  }

  /**
   * 可用的上下文
   *
   * @return
   */
  default Set<Class<? extends BaseSettingContext>> avaliableSettingContext() {
    return Collections.emptySet();
  }

}
