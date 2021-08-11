package mydataharbor;

import mydataharbor.executor.AbstractDataExecutor;
import mydataharbor.executor.CommonDataExecutor;
import mydataharbor.setting.BaseSettingContext;

import java.io.Closeable;
import java.lang.reflect.Type;

/**
 * 单条数据处理流程
 *
 * @param <P> 协议数据
 * @param <C> 转换数据
 * @auth xulang
 * @Date 2021/4/29
 **/

public interface IDataPipline<T, P extends IProtocalData, R, S extends BaseSettingContext> extends IData, Closeable {

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
  IProtocalDataConvertor<T, P, S> protocalDataConvertor();

  /**
   * 提供消息转换前的检查器
   *
   * @return
   */
  IProtocalDataChecker checker();

  /**
   * 协议数据转换
   *
   * @return
   */
  IDataConvertor<P, R, S> dataConventer();

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
    return getTypeByIndex(0, "T", IDataPipline.class);
  }

  default Type getPType() {
    return getTypeByIndex(1, "P", IDataPipline.class);
  }

  default Type getRType() {
    return getTypeByIndex(2, "R", IDataPipline.class);
  }

  default Type getSType() {
    return getTypeByIndex(3, "S", IDataPipline.class);
  }


}
