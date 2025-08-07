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

import mydataharbor.executor.AbstractDataExecutor;
import mydataharbor.executor.CommonDataExecutor;
import mydataharbor.setting.BaseSettingContext;

import java.io.Closeable;
import java.lang.reflect.Type;

/**
 * 单条数据处理流程
 * @param <T> 驱动原始介质数据
 * @param <P> 协议数据
 * @param <R> 转换数据
 * @param <S> 任务设置
 * @auth xulang
 * @Date 2021/4/29
 **/

public interface IDataPipeline<T, P extends IProtocolData, R, S extends BaseSettingContext> extends IData, Closeable {

  /**
   * 获得数据提供器
   *
   * @return
   */
  IDataSource<T, S> dataSource();

  /**
   * 协议转换器
   *
   * @return
   */
  IProtocolDataConverter<T, P, S> protocolDataConverter();

  /**
   * 提供消息转换前的检查器
   *
   * @return
   */
  IProtocolDataChecker checker();

  /**
   * 协议数据转换
   *
   * @return
   */
  IDataConverter<P, R, S> dataConverter();

  /**
   * 提供数据写入器
   *
   * @return
   */
  IDataSink<R, S> sink();

  /**
   * 配置上下文
   *
   * @return
   */
  S settingContext();

  /**
   * 指定执行器
   *
   * @return
   */
  default Class<? extends AbstractDataExecutor> pointExecutorType() {
    return CommonDataExecutor.class;
  }

  default String threadNameGenerate(String taskId, int index) {
    return taskId + "-" + index;
  }

  default Type getTType() {
    return getTypeByIndex(0, "T", IDataPipeline.class);
  }

  default Type getPType() {
    return getTypeByIndex(1, "P", IDataPipeline.class);
  }

  default Type getRType() {
    return getTypeByIndex(2, "R", IDataPipeline.class);
  }

  default Type getSType() {
    return getTypeByIndex(3, "S", IDataPipeline.class);
  }


}