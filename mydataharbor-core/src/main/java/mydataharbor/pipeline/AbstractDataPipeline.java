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


package mydataharbor.pipeline;

import lombok.NonNull;
import mydataharbor.AbstractDataChecker;
import mydataharbor.IDataConverter;
import mydataharbor.IDataPipeline;
import mydataharbor.IDataSink;
import mydataharbor.IDataSource;
import mydataharbor.IProtocolData;
import mydataharbor.IProtocolDataConverter;
import mydataharbor.setting.BaseSettingContext;

import java.io.IOException;

/**
 * @auth xulang
 * @Date 2021/5/7
 **/

public abstract class AbstractDataPipeline<T, P extends IProtocolData, R, S extends BaseSettingContext> implements IDataPipeline<T, P, R, S> {

  protected IDataSource<T, S> dataSource;

  protected IProtocolDataConverter<T, P, S> protocolDataConverter;

  protected AbstractDataChecker checker;

  protected IDataConverter<P, R, S> dataConverter;

  protected IDataSink<R, S> sink;

  protected S settingContext;

  public AbstractDataPipeline(
    @NonNull IDataSource<T, S> dataSource,
    @NonNull IProtocolDataConverter<T, P, S> protocolDataConverter,
    AbstractDataChecker checker,
    @NonNull IDataConverter<P, R, S> dataConverter,
    @NonNull IDataSink<R, S> sink,
    @NonNull S settingContext) {
    this.dataSource = dataSource;
    this.protocolDataConverter = protocolDataConverter;
    this.checker = checker;
    this.dataConverter = dataConverter;
    this.sink = sink;
    this.settingContext = settingContext;
  }

  @Override
  public IDataSource<T, S> dataSource() {
    return dataSource;
  }

  @Override
  public IProtocolDataConverter<T, P, S> protocolDataConverter() {
    return protocolDataConverter;
  }

  @Override
  public AbstractDataChecker checker() {
    return checker;
  }

  @Override
  public IDataConverter<P, R, S> dataConverter() {
    return dataConverter;
  }

  @Override
  public IDataSink<R, S> sink() {
    return sink;
  }

  @Override
  public S settingContext() {
    return settingContext;
  }

  @Override
  public void close() throws IOException {
    dataSource.close();
    sink.close();
  }
}