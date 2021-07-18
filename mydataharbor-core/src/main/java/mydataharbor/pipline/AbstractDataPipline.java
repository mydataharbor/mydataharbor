package mydataharbor.pipline;

import mydataharbor.*;
import mydataharbor.setting.BaseSettingContext;
import lombok.NonNull;

import java.io.IOException;

/**
 * @auth xulang
 * @Date 2021/5/7
 **/

public abstract class AbstractDataPipline<T, P extends IProtocalData, R, S extends BaseSettingContext> implements IDataPipline<T, P, R, S> {

  protected IDataSource<T, S> dataSource;

  protected IDataProtocalConvertor<T, P, S> dataProtocalConventor;

  protected AbstractDataChecker checker;

  protected IDataConvertor<P, R, S> dataConventor;

  protected IDataSink<R, S> sink;

  protected S settingContext;

  public AbstractDataPipline(
    @NonNull IDataSource<T, S> dataSource,
    @NonNull IDataProtocalConvertor<T, P, S> dataProtocalConventor,
    AbstractDataChecker checker,
    @NonNull IDataConvertor<P, R, S> dataConventor,
    @NonNull IDataSink<R, S> sink,
    @NonNull S settingContext) {
    this.dataSource = dataSource;
    this.dataProtocalConventor = dataProtocalConventor;
    this.checker = checker;
    this.dataConventor = dataConventor;
    this.sink = sink;
    this.settingContext = settingContext;
  }

  @Override
  public IDataSource<T, S> dataSource() {
    return dataSource;
  }

  @Override
  public IDataProtocalConvertor<T, P, S> dataProtocalConventor() {
    return dataProtocalConventor;
  }

  @Override
  public AbstractDataChecker checker() {
    return checker;
  }

  @Override
  public IDataConvertor<P, R, S> dataConventer() {
    return dataConventor;
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
