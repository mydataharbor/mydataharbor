package mydataharbor;

import mydataharbor.exception.TheEndException;
import mydataharbor.setting.BaseSettingContext;

import java.io.Closeable;
import java.lang.reflect.Type;

/**
 * 数据港口数据源接口
 * 该类会作为一个线程启动
 *
 * @author xulang
 */
public interface IDataSource<T, S extends BaseSettingContext> extends Closeable, IData {

  /**
   * 数据提供器类型
   *
   * @return
   */
  String dataSourceType();

  /**
   * 获取数据，循环调用
   * 当数据处理完成是，该方法将抛出
   *
   * @return
   * @see TheEndException
   */
  Iterable<T> poll(S settingContext) throws TheEndException;

  /**
   * 提交数据
   * <p>
   * 如果开启多线程处理，单条写入，则需要此方法线程安全
   *
   * @param record
   */
  void commit(T record, S settingContext);

  /**
   * 批量提交
   *
   * @param records
   */
  void commit(Iterable<T> records, S settingContext);

  /**
   * 回滚
   * 如果开启多线程处理，单条写入，则需要此方法线程安全
   *
   * @param record
   */
  void rollback(T record, S settingContext);

  /**
   * 回滚一批
   *
   * @param records
   */
  void rollback(Iterable<T> records, S settingContext);

  /**
   * 计算事务回滚单元
   * 返回能标识本次处理的记录所在的事务单元
   * 比如kafka 就是 partition id，返回partition id一个常量即可
   * 如果数据源支持单个消息的事务，那么就返回消息自身即可
   * 这里计算的依据是，该条消息回滚的影响范围，如果消息独立成事务那么返回自身即可，如果消息直接有事务关联性比如kafka它的事务粒度是partition，那么就返回partition号即可
   * 原则就是返回能反映该条消息所处的事务粒度
   *
   * @return
   */
  default Object rollbackTransactionUnit(T record) {
    return record;
  }

  /**
   * TODO 处理线程路由
   *
   * 当单管道开启多线程处理时，框架会调用该方法决定把这个任务分配给哪个线程处理，保证处理的顺序性
   *
   * @param record
   * @return 框架会使用该返回值执行hash进行路由
   */
  default Object threadRouter(T record) {
    return record;
  }


  default Type getTType() {
    return getTypeByIndex(0, "T", IDataSource.class);
  }

  default Type getSType() {
    return getTypeByIndex(1, "S", IDataSource.class);
  }


}
