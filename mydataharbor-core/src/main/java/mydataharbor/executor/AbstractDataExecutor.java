package mydataharbor.executor;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.*;
import mydataharbor.exception.ResetException;
import mydataharbor.exception.TheEndException;
import mydataharbor.monitor.TaskExecutorMonitor;
import mydataharbor.setting.BaseSettingContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @param <T> 原始介质数据
 * @param <P> 平台协议数据
 * @param <R> writer数据
 * @param <S> 配置上下文
 * @auth xulang
 * @Date 2021/4/30
 **/
@Slf4j
public abstract class AbstractDataExecutor<T, P extends IProtocalData, R, S extends BaseSettingContext> extends Thread implements Closeable {

  private IDataPipline<T, P, R, S> dataPipline;

  private S settingContext;

  /**
   * 是否结束
   */
  private volatile boolean run = true;

  /**
   * 是否暂停
   */
  private volatile boolean suspend = false;

  /**
   * 线程是否结束
   */
  private volatile boolean end = true;

  /**
   * 处理计数器
   */
  private volatile AtomicLong writeCount = new AtomicLong();

  private List<IExecutorListener> executorListeners = new CopyOnWriteArrayList<>();

  /**
   * 并行处理线程池
   */
  protected ForkJoinPool forkJoinPool;

  private Map<Object, Boolean> rollbackUnit;

  private TaskExecutorMonitor taskmonitor;

  public AbstractDataExecutor(IDataPipline<T, P, R, S> dataPipline, String threadName) {
    this.dataPipline = dataPipline;
    this.settingContext = dataPipline.settingContext();
    setName(threadName);
  }

  private void safeListenerRun(ISafeRun run) {
    try {
      run.run();
    } catch (Throwable throwable) {
      log.error("通知listener时发生异常", throwable);
    }
  }

  public void addListener(IExecutorListener executorListener) {
    executorListeners.add(executorListener);
  }

  public IDataPipline<T, P, R, S> getDataPipline() {
    return dataPipline;
  }

  /**
   * 数据产生处
   */
  @Override
  public void run() {
    end = false;
    taskmonitor.setEnd(end);
    if (!suspend)
      safeListenerRun(() -> executorListeners.stream().forEach(listener -> listener.onRun(this, dataPipline)));
    IDataSource<T, S> dataSource = dataPipline.dataSource();
    taskmonitor.setTotal(dataSource.total());
    IProtocalDataConvertor<T, P, S> protocalDataConvertor = dataPipline.protocalDataConvertor();
    IProtocalDataChecker<P, S> checker = dataPipline.checker();
    IDataConvertor<P, R, S> dataConvertor = dataPipline.dataConventer();
    IDataSink<R, S> sink = dataPipline.sink();
    try {
      while (run) {
        while (suspend) {
          if (!run) {
            //允许暂停时被结束
            break;
          }
          taskmonitor.setLastRunTime(System.currentTimeMillis());
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            log.error("暂停被打断");
          }
        }
        if (!run) {
          break;
        }
        try {
          doRun(dataSource, protocalDataConvertor, checker, dataConvertor, sink);
        } catch (TheEndException e) {
          //数据拉取完毕
          //跳出循环
          break;
        } finally {
          taskmonitor.setLastRunTime(System.currentTimeMillis());
        }
        if (settingContext.getSleepTime() != 0L) {
          try {
            Thread.sleep(settingContext.getSleepTime());
          } catch (InterruptedException e) {
            log.error("睡眠被打断", e);
          }
        }
      }
      safeListenerRun(() -> executorListeners.stream().forEach(listener -> listener.onSucccessEnd(this, dataPipline, writeCount.longValue(), run)));
    } catch (Throwable e) {
      log.error("发生未知异常任务线程异常退出", e);
      safeListenerRun(() -> executorListeners.stream().forEach(listener -> listener.onExceptionEnd(this, dataPipline, e, writeCount.longValue())));
    } finally {
      end = true;
      taskmonitor.setEnd(end);
      if (run)
        safeListenerRun(this::close);
      if (forkJoinPool != null) {
        forkJoinPool.shutdown();
      }
      log.info("{}该线程结束！", getName());
    }

  }

  private void doRun(IDataSource<T, S> dataProvider, IProtocalDataConvertor<T, P, S> protocalDataConvertor, IProtocalDataChecker checker, IDataConvertor<P, R, S> dataConvertor, IDataSink<R, S> writer) throws TheEndException {
    //协议转换通过的数据
    List<P> protocalConventSuccess = Collections.synchronizedList(new ArrayList<>());
    //协议转换失败的数据
    List<ErrorRecord<T, Object>> protocalConventError = Collections.synchronizedList(new ArrayList<>());
    //检查通过的数据
    List<P> checkerSuccess = Collections.synchronizedList(new ArrayList<>());
    //检查失败的数据
    List<ErrorRecord<P, AbstractDataChecker.CheckResult>> checkerError = Collections.synchronizedList(new ArrayList<>());
    //转换通过的数据
    List<R> dataConventSuccess = Collections.synchronizedList(new ArrayList<>());
    //转换失败的数据
    List<ErrorRecord<P, Object>> dataConventError = Collections.synchronizedList(new ArrayList<>());
    //写入成功
    List<R> writeSuccess = Collections.synchronizedList(new ArrayList<>());
    //写入失败
    List<ErrorRecord<R, IDataSink.WriterResult>> writeError = Collections.synchronizedList(new ArrayList<>());
    //进入write流程的原始记录
    List<T> tRecordConventSucces = Collections.synchronizedList(new ArrayList<>());
    Iterable<T> tRecordsIterable = dataPipline.dataSource().poll(settingContext);
    boolean empty = !tRecordsIterable.iterator().hasNext();
    if (empty) {
      return;
    }
    rollbackUnit = new ConcurrentHashMap<>();
    Stream<T> stream = StreamSupport.stream(tRecordsIterable.spliterator(), settingContext.isParallel());
    //开启了并行，并且制定了线程数
    if (settingContext.isParallel() && settingContext.getThreadNum() != 0) {
      if (forkJoinPool == null)
        forkJoinPool = new ForkJoinPool(settingContext.getThreadNum());
      try {
        forkJoinPool.submit(() -> {
          forEach(stream, dataProvider, protocalDataConvertor, checker, dataConvertor, writer, protocalConventSuccess, protocalConventError, checkerSuccess, checkerError, dataConventSuccess, dataConventError, writeSuccess, writeError, tRecordConventSucces);
        }).get();
      } catch (InterruptedException | ExecutionException e) {
        log.error("并行执行任务发生异常！", e);
      }
    } else {
      forEach(stream, dataProvider, protocalDataConvertor, checker, dataConvertor, writer, protocalConventSuccess, protocalConventError, checkerSuccess, checkerError, dataConventSuccess, dataConventError, writeSuccess, writeError, tRecordConventSucces);
    }
    log.info("原始数据源数据：{}", tRecordsIterable);
    if (tRecordsIterable instanceof Collection) {
      taskmonitor.addAndGettRecordCount((long) ((Collection) tRecordsIterable).size());
    } else {
      tRecordsIterable.forEach((record) -> taskmonitor.addAndGettRecordCount(1L));
    }
    //日志记录
    log.info("协议转换通过记录:{}", protocalConventSuccess);
    taskmonitor.addAndGetProtocalConventSuccessCount((long) protocalConventSuccess.size());
    if (!protocalConventError.isEmpty()) {
      log.info("协议转换失败记录:{}", protocalConventError);
      taskmonitor.addAndGetProtocalConventErrorCount((long) protocalConventError.size());
    }
    log.info("检查通过记录:{}", checkerSuccess);
    taskmonitor.addAndGetCheckerSuccessCount((long) checkerSuccess.size());
    if (!checkerError.isEmpty()) {
      log.info("检查失败记录:{}", checkerError);
      taskmonitor.addAndGetCheckerErrorCount((long) checkerError.size());
    }
    log.info("数据转换通过记录:{}", dataConventSuccess);
    taskmonitor.addAndGetDataConventSuccessCount((long) dataConventSuccess.size());
    if (!dataConventError.isEmpty()) {
      log.info("数据转换失败记录:{}", dataConventError);
      taskmonitor.addAndGetDataConventErrorCount((long) dataConventError.size());

    }
    //检查错误列表里是否有reset异常，如果有则放弃此次写入和提交
    if (isContainRestException(protocalConventError, checkerError, dataConventError)) {
      log.error("写入前流程发生reset异常！");
      tRecordsIterable.forEach(record -> rollbackUnit.put(dataProvider.rollbackTransactionUnit(record), true));
      dataProvider.rollback(tRecordsIterable, settingContext);
      return;
    } else if (tRecordConventSucces.size() == 0) {
      //数据全部转换失败，并且无需回滚
      dataProvider.commit(tRecordsIterable, settingContext);
    } else {
      //批量数据写入
      if (settingContext.isBatchWrite()) {
        batchWrite(dataProvider, writer, dataConventSuccess, writeSuccess, writeError, tRecordConventSucces);
      } else if (settingContext.isBatchCommit()) {
        //单条写入，批量提交
        dataProvider.commit(tRecordConventSucces, settingContext);
      }
    }
    log.info("写入成功记录:{}", writeSuccess);
    writeCount.addAndGet(writeSuccess.size());
    taskmonitor.addAndGetWriteSuccessCount((long) writeSuccess.size());
    if (!writeError.isEmpty()) {
      log.info("写入失败记录:{}", writeError);
      taskmonitor.addAndGetWriteErrorCount((long) writeError.size());
    }
  }

  /**
   * 暂停
   */
  public void pause() {
    this.suspend = true;
    taskmonitor.setSuspend(suspend);
    safeListenerRun(() -> executorListeners.stream().forEach(listener -> listener.onSuspend(this, dataPipline, writeCount.longValue())));
  }

  /**
   * 继续
   */
  public void doContinue() {
    this.suspend = false;
    taskmonitor.setSuspend(suspend);
    safeListenerRun(() -> executorListeners.stream().forEach(listener -> listener.onContinue(this, dataPipline, writeCount.longValue())));
  }

  protected void forEach(Stream<T> stream, IDataSource<T, S> dataProvider, IProtocalDataConvertor<T, P, S> rotocalDataConvertor, IProtocalDataChecker checker, IDataConvertor<P, R, S> dataConvertor, IDataSink<R, S> writer, List<P> protocalConventSuccess, List<ErrorRecord<T, Object>> protocalConventError, List<P> checkerSuccess, List<ErrorRecord<P, IProtocalDataChecker.CheckResult>> checkerError, List<R> dataConventSuccess, List<ErrorRecord<P, Object>> dataConventError, List<R> writeSuccess, List<ErrorRecord<R, IDataSink.WriterResult>> writeError, List<T> tRecordConventSucces) {
    stream.forEach(tRecord -> {
      Object rollbackTransactionUnit = dataProvider.rollbackTransactionUnit(tRecord);
      if (!rollbackUnit.getOrDefault(rollbackTransactionUnit, false) || settingContext.isContinueOnRollbackOccurContinueInOncePoll())
        doForEach(dataProvider, rotocalDataConvertor, checker, dataConvertor, writer, protocalConventSuccess, protocalConventError, checkerSuccess, checkerError, dataConventSuccess, dataConventError, writeSuccess, writeError, tRecordConventSucces, tRecord);
    });
  }


  /**
   * 处理单条数据
   *
   * @param dataProvider
   * @param protocalDataConvertor
   * @param checker
   * @param dataConvertor
   * @param writer
   * @param protocalConventSuccess
   * @param protocalConventError
   * @param checkerSuccess
   * @param checkerError
   * @param dataConventSuccess
   * @param dataConventError
   * @param writeSuccess
   * @param writeError
   * @param tRecordConventSucces
   * @param tRecord
   * @return
   */
  protected void doForEach(IDataSource<T, S> dataProvider, IProtocalDataConvertor<T, P, S> protocalDataConvertor, IProtocalDataChecker checker, IDataConvertor<P, R, S> dataConvertor, IDataSink<R, S> writer, List<P> protocalConventSuccess, List<ErrorRecord<T, Object>> protocalConventError, List<P> checkerSuccess, List<ErrorRecord<P, IProtocalDataChecker.CheckResult>> checkerError, List<R> dataConventSuccess, List<ErrorRecord<P, Object>> dataConventError, List<R> writeSuccess, List<ErrorRecord<R, IDataSink.WriterResult>> writeError, List<T> tRecordConventSucces, T tRecord) {
    //协议转换
    P protocalData = protocalConvent(protocalDataConvertor, protocalConventSuccess, protocalConventError, tRecord);
    if (protocalData == null) {
      //协议转换失败
      return;
    }
    if (checker != null) {
      //数据检查
      AbstractDataChecker.CheckResult checkResult = protocalDataCheck(checker, checkerSuccess, checkerError, protocalData);
      if (!checkResult.isPass()) {
        //数据检查未通过
        return;
      }
    } else {
      log.info("checker没有配置");
      checkerSuccess.add(protocalData);
    }
    //数据转换
    List<R> records = dataConvent(dataConvertor, dataConventSuccess, dataConventError, protocalData);
    if (records == null) {
      //数据转换失败
      log.error("数据转换失败!");
      return;
    }
    tRecordConventSucces.add(tRecord);
    //单条数据写入
    if (!settingContext.isBatchWrite()) {
      singleRecordWrite(dataProvider, writer, writeSuccess, writeError, tRecord, records);
    }
    return;
  }

  /**
   * 是否存在reset exception
   *
   * @param errorRecordList
   * @return
   */
  private boolean isContainRestException(List<?>... errorRecordList) {
    for (List<?> errorRecords : errorRecordList) {
      for (Object errorRecord : errorRecords) {
        ErrorRecord errorRecord1 = (ErrorRecord) errorRecord;
        if (errorRecord1.getUnknownException() != null && errorRecord1.getUnknownException() instanceof ResetException) {
          return true;
        }
      }
    }
    return false;
  }

  private void batchWrite(IDataSource<T, S> dataSource, IDataSink<R, S> sink, List<R> dataConventSuccess, List<R> writeSuccess, List<ErrorRecord<R, IDataSink.WriterResult>> writeError, List<T> tRecordConventSucces) {
    try {
      IDataSink.WriterResult writeResult = sink.write(dataConventSuccess, settingContext);
      if (writeResult.isSuccess()) {
        writeSuccess.addAll(dataConventSuccess);
      } else {
        List<ErrorRecord<R, IDataSink.WriterResult>> writeErrorRecords = dataConventSuccess.stream().map(record -> {
          return ErrorRecord.<R, IDataSink.WriterResult>builder()
            .record(record)
            .knownError(writeResult).build();
        }).collect(Collectors.toList());
        writeError.addAll(writeErrorRecords);
      }
      if (writeResult.isCommit()) {
        try {
          //防止commit异常
          dataSource.commit(tRecordConventSucces, settingContext);
        } catch (Exception e) {
          throw new ResetException("commit异常", e);
        }
      } else {
        tRecordConventSucces.forEach(record -> {
          rollbackUnit.put(dataSource.rollbackTransactionUnit(record), true);
        });
        dataSource.rollback(tRecordConventSucces, settingContext);
      }
    } catch (Exception e) {
      log.error("批量写入异常！", e);
      if (!(e instanceof ResetException)) {
        dataSource.commit(tRecordConventSucces, settingContext);
      } else {
        //这里认为回滚不会有异常情况
        tRecordConventSucces.forEach(record -> {
          rollbackUnit.put(dataSource.rollbackTransactionUnit(record), true);
        });
        dataSource.rollback(tRecordConventSucces, settingContext);
      }
      List<ErrorRecord<R, IDataSink.WriterResult>> writeErrorRecords = dataConventSuccess.stream().map(record -> {
        return ErrorRecord.<R, IDataSink.WriterResult>builder()
          .record(record)
          .unknownException(e).build();
      }).collect(Collectors.toList());
      writeError.addAll(writeErrorRecords);
    }
  }

  private void singleRecordWrite(IDataSource<T, S> dataSource, IDataSink<R, S> sink, List<R> writeSuccess, List<ErrorRecord<R, IDataSink.WriterResult>> writeError, T tRecord, List<R> records) {
    //单条写入
    try {

      IDataSink.WriterResult writerResult;
      if (records.size() == 1) {
        writerResult = sink.write(records.get(0), settingContext);
      } else {
        writerResult = sink.write(records, settingContext);
      }
      if (writerResult.isSuccess()) {
        writeSuccess.addAll(records);
      } else {
        for (R record : records) {
          writeError.add(ErrorRecord.<R, IDataSink.WriterResult>builder()
            .record(record)
            .knownError(writerResult).build());
        }
      }
      if (writerResult.isCommit()) {
        //数据提交
        try {
          //防止commit异常
          if (!settingContext.isBatchCommit())
            dataSource.commit(tRecord, settingContext);
        } catch (Exception e) {
          throw new ResetException("commit异常", e);
        }
      } else {
        //数据回滚
        //这里认为回滚不会有异常情况
        rollbackUnit.put(dataSource.rollbackTransactionUnit(tRecord), true);
        dataSource.rollback(tRecord, settingContext);
      }

    } catch (Exception e) {
      log.error("单条写入异常", e);
      if (!(e instanceof ResetException)) {
        //提交数据
        if (!settingContext.isBatchCommit())
          dataSource.commit(tRecord, settingContext);
      } else {
        //回滚数据
        rollbackUnit.put(dataSource.rollbackTransactionUnit(tRecord), true);
        dataSource.rollback(tRecord, settingContext);
      }
      for (R record : records) {
        writeError.add(ErrorRecord.<R, IDataSink.WriterResult>builder()
          .record(record)
          .unknownException(e).build());
      }
    }
  }

  private List<R> dataConvent(IDataConvertor<P, R, S> dataConvertor, List<R> dataConventSuccess, List<ErrorRecord<P, Object>> dataConventError, P protocalData) {
    try {
      Object record = dataConvertor.convert(protocalData, settingContext);
      if (record instanceof List) {
        dataConventSuccess.addAll((List) record);
        return (List) record;
      }
      dataConventSuccess.add((R) record);
      return Collections.singletonList((R) record);
    } catch (Exception e) {
      log.error("数据转换异常！", e);
      dataConventError.add(ErrorRecord.<P, Object>builder()
        .record(protocalData)
        .unknownException(e)
        .build());
    }
    return null;
  }

  private AbstractDataChecker.CheckResult protocalDataCheck(IProtocalDataChecker checker, List<P> checkerSuccess, List<ErrorRecord<P, AbstractDataChecker.CheckResult>> checkerError, P protocalData) {
    try {
      AbstractDataChecker.CheckResult checkResult = checker.check(null, protocalData, settingContext);
      if (checkResult.isPass()) {
        checkerSuccess.add(protocalData);
      } else {
        checkerError.add(ErrorRecord.<P, AbstractDataChecker.CheckResult>builder()
          .record(protocalData)
          .knownError(checkResult)
          .build());
      }
      return checkResult;
    } catch (Exception e) {
      log.error("校验发生异常！", e);
      checkerError.add(ErrorRecord.<P, AbstractDataChecker.CheckResult>builder()
        .record(protocalData)
        .unknownException(e)
        .build());
    }
    return null;
  }

  private P protocalConvent(IProtocalDataConvertor<T, P, S> protocalDataConvertor, List<P> protocalConventSuccess, List<ErrorRecord<T, Object>> protocalConventError, T tRecord) {
    try {
      P protocalData = protocalDataConvertor.convert(tRecord, settingContext);
      protocalConventSuccess.add(protocalData);
      return protocalData;
    } catch (Exception e) {
      log.error("协议转换失败！", e);
      protocalConventError.add(ErrorRecord.<T, Object>builder()
        .record(tRecord)
        .unknownException(e)
        .build());
    }
    return null;
  }

  @Override
  public void close() throws IOException {
    run = false;
    taskmonitor.setRun(run);
    safeListenerRun(() -> executorListeners.stream().forEach(listener -> listener.onClose(this, dataPipline, writeCount.longValue(), run)));
    while (!end) {
      //等待工作线程结束
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
        log.error("线程被打断！");
      }
    }
    dataPipline.close();
  }

  public void setTaskMonitorMBean(TaskExecutorMonitor taskmonitor) {
    this.taskmonitor = taskmonitor;
  }
}
