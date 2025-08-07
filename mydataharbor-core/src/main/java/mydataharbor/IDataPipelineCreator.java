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


package mydataharbor;

import mydataharbor.setting.BaseSettingContext;

import java.util.Collections;
import java.util.Set;

/**
 * pipeline创建器
 *
 * @auth xulang
 * @Date 2021/5/8
 **/
public interface IDataPipelineCreator<C, S extends BaseSettingContext> extends IData {

  /**
   * 创建器名称
   *
   * @return
   */
  String type();

  /**
   * 通过配置创建pipeline
   *
   * @param config
   * @return
   */
  IDataPipeline createPipeline(C config, S settingContext) throws Exception;

  /**
   * 是否可以创建pipeline，有些creator只提供可用的资源
   *
   * @return
   */
  default boolean canCreatePipeline() {
    return true;
  }


  /**
   * 获得config类型
   *
   * @return
   */
  default Class<C> getConfigClass() {
    return getTypeByIndex(0, "C", IDataPipelineCreator.class);
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
    return getTypeByIndex(1, "S", IDataPipelineCreator.class);
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
  default Set<Class<? extends IProtocolDataConverter>> availableProtocolDataConverter() {
    return Collections.emptySet();
  }

  /**
   * 可用的数据检查器
   *
   * @return
   */
  default Set<Class<? extends AbstractDataChecker>> availableDataChecker() {
    return Collections.emptySet();
  }


  /**
   * 可用的数据转换器
   *
   * @return
   */
  default Set<Class<? extends IDataConverter>> availableDataConverter() {
    return Collections.emptySet();
  }


  /**
   * 可用的数据写入器
   *
   * @return
   */
  default Set<Class<? extends IDataSink>> availableDataSink() {
    return Collections.emptySet();
  }

  /**
   * 可用的上下文
   *
   * @return
   */
  default Set<Class<? extends BaseSettingContext>> availableSettingContext() {
    return Collections.emptySet();
  }

}