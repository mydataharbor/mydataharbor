package mydataharbor.plugin.app.task;

import mydataharbor.IDataPipline;
import mydataharbor.IDataSinkCreator;
import mydataharbor.IExecutorListener;
import mydataharbor.datasource.AbstractRateLimitDataSource;
import mydataharbor.datasource.RateLimitConfig;
import mydataharbor.executor.AbstractDataExecutor;
import mydataharbor.monitor.Taskmonitor;
import mydataharbor.plugin.api.IPluginInfoManager;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.api.ITaskManager;
import mydataharbor.plugin.api.exception.PiplineCreateException;
import mydataharbor.plugin.api.exception.TaskManageException;
import mydataharbor.plugin.api.task.SingleTask;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.plugin.app.listener.ExecutorListener;
import mydataharbor.setting.BaseSettingContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
@Slf4j
public class TaskManager implements ITaskManager {

  private IPluginInfoManager pluginInfoManager;

  private IPluginServer pluginServer;

  private CuratorFramework client;

  public TaskManager(IPluginInfoManager pluginInfoManager, CuratorFramework client, IPluginServer pluginServer) {
    this.pluginInfoManager = pluginInfoManager;
    this.pluginServer = pluginServer;
    this.client = client;
  }

  /**
   * pipline缓存
   */
  private Map<String, List<IDataPipline>> piplineMap = new ConcurrentHashMap<>();

  /**
   * 任务状态
   */
  private Map<String, TaskState> taskStateMap = new ConcurrentHashMap<>();

  /**
   * 所有提交的task
   */
  private volatile Map<String, SingleTask> taskMap = new ConcurrentHashMap<>();

  /**
   * executor缓存
   */
  private Map<String, List<AbstractDataExecutor>> executorMap = new ConcurrentHashMap<>();

  private Map<String, IExecutorListener> executorListenerMap = new ConcurrentHashMap<>();


  @Override
  public String submitTask(SingleTask singleTask) {
    List<IDataPipline> dataPiplines = piplineMap.get(singleTask.getTaskId());
    if (dataPiplines != null) {
      throw new PiplineCreateException("该任务号已经下发！");
    }
    ExecutorListener executorListener = new ExecutorListener(singleTask.getTaskId(), client, pluginServer.getNodeInfo());
    executorListenerMap.put(singleTask.getTaskId(), executorListener);
    try {
      IDataSinkCreator dataSinkCreator = getDataSinkCreator(singleTask);
      dataPiplines = new ArrayList<>();
      List<AbstractDataExecutor> executors = new ArrayList<>();
      doCreatePipline(0, singleTask.getNumberOfPipline(), singleTask, dataSinkCreator, dataPiplines, executors, executorListener);
      piplineMap.put(singleTask.getTaskId(), dataPiplines);
      executorMap.put(singleTask.getTaskId(), executors);
      taskStateMap.put(singleTask.getTaskId(), TaskState.created);
      taskMap.put(singleTask.getTaskId(), singleTask);
      executorListener.onPiplineCreate(0, singleTask.getNumberOfPipline(), null);
    } catch (Throwable throwable) {
      executorListener.onPiplineCreate(0, singleTask.getNumberOfPipline(), throwable);
      throw throwable;
    }

    return singleTask.getTaskId();
  }

  @NotNull
  private IDataSinkCreator getDataSinkCreator(SingleTask singleTask) {
    if (StringUtils.isBlank(singleTask.getTaskId()) || singleTask.getNumberOfPipline() < 0 || StringUtils.isBlank(singleTask.getPluginId()) || StringUtils.isBlank(singleTask.getMydataharborCreatorClazz()) || StringUtils.isBlank(singleTask.getConfigJson()) || StringUtils.isBlank(singleTask.getSettingJsonConfig())) {
      throw new PiplineCreateException("各参数不能为空或者为0校验不通过，请检查");
    }
    Map<String, IDataSinkCreator> stringIDataSinkCreatorMap = pluginInfoManager.getDataSinkCreatorMapByPlugin(singleTask.getPluginId());
    if (stringIDataSinkCreatorMap == null) {
      throw new PiplineCreateException("该机器没有安装这个插件，或者这个插件没有任何creator：" + singleTask.getPluginId());
    }
    IDataSinkCreator dataSinkCreator = stringIDataSinkCreatorMap.get(singleTask.getMydataharborCreatorClazz());
    if (dataSinkCreator == null) {
      throw new PiplineCreateException("没有这个creator：" + singleTask.getMydataharborCreatorClazz());
    }
    return dataSinkCreator;
  }

  private void doCreatePipline(int begin, int number, SingleTask singleTask, IDataSinkCreator dataSinkCreator, List<IDataPipline> dataPiplines, List<AbstractDataExecutor> executors, IExecutorListener executorListener) {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(dataSinkCreator.getClass().getClassLoader());
      for (int i = begin; i < number + begin; i++) {
        log.info("任务号:{},第{}个pipline开始创建", singleTask.getTaskId(), i + 1);
        IDataPipline dataPipline = dataSinkCreator.createPipline(dataSinkCreator.parseJson(singleTask.getConfigJson(), dataSinkCreator.getConfigClass()), (BaseSettingContext) dataSinkCreator.parseJson(singleTask.getSettingJsonConfig(), dataSinkCreator.getSettingClass()));
        generateRateGroup(singleTask, dataPipline);
        log.info("任务号:{},第{}个pipline创建完毕:{}", singleTask.getTaskId(), i + 1, dataPipline);
        dataPiplines.add(dataPipline);
        log.info("开始创建executor");
        Class<? extends AbstractDataExecutor> pointExecutorType = dataPipline.pointExecutorType();
        log.debug("使用+" + pointExecutorType + "创建");
        try {
          Constructor<? extends AbstractDataExecutor> constructor = pointExecutorType.getConstructor(IDataPipline.class, String.class);
          AbstractDataExecutor abstractDataExecutor = constructor.newInstance(dataPipline, dataPipline.threadNameGenerate(singleTask.getTaskId(), i + 1));
          abstractDataExecutor.addListener(executorListener);
          abstractDataExecutor.setTaskMonitorMBean(new Taskmonitor(singleTask.getTaskId(), abstractDataExecutor.getName()));
          executors.add(abstractDataExecutor);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
          throw new PiplineCreateException("pipline指定的executor执行器构造方法不符合规范:" + e.getMessage(), e);
        }
        log.info("executor创建完毕！");
      }
    } catch (Exception e) {
      log.error("依据参数创建pipline发送异常！", e);
      throw new PiplineCreateException("依据参数创建pipline发生异常:" + e.getMessage(), e);
    } finally {
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  /**
   * 设置限速组
   *
   * @param singleTask
   * @param dataPipline
   */
  private void generateRateGroup(SingleTask singleTask, IDataPipline dataPipline) {
    if (dataPipline.dataSource() instanceof AbstractRateLimitDataSource) {
      AbstractRateLimitDataSource abstractRateLimitDataSource = (AbstractRateLimitDataSource) dataPipline.dataSource();
      RateLimitConfig rateLimit = abstractRateLimitDataSource.getRateLimitConfig();
      if (StringUtils.isBlank(rateLimit.getRateGroup())) {
        rateLimit.setRateGroup(singleTask.getTaskId());
      }
    }
  }

  @Override
  public TaskState queryTaskState(String taskId) {
    return taskStateMap.get(taskId);
  }

  @Override
  public List<SingleTask> lisTask() {
    return new ArrayList<>(taskMap.values());
  }

  @Override
  public void manageTask(String taskId, TaskState taskState) {
    TaskState nowState = this.taskStateMap.get(taskId);
    if (nowState == null)
      return;
    if (nowState.equals(taskState))
      return;
    List<AbstractDataExecutor> executors = executorMap.get(taskId);
    if (executors == null) {
      throw new TaskManageException("该task id 还没有创建pipline，无法管理状态");
    }
    switch (taskState) {
      case created:
        //创建
        throw new TaskManageException("创建请使用createPipline方法");
      case started:
        if (nowState != TaskState.created) {
          throw new TaskManageException("开始前状态不合法，当前状态：" + nowState + "期望：" + TaskState.created);
        }
        //启动
        for (AbstractDataExecutor executor : executors) {
          executor.start();
        }
        taskStateMap.put(taskId, TaskState.started);
        break;
      case suspend:
        if (nowState != TaskState.started) {
          throw new TaskManageException("当前状态不是正在运行，无法暂停");
        }
        for (AbstractDataExecutor executor : executors) {
          executor.pause();
        }
        taskStateMap.put(taskId, TaskState.suspend);
        break;
      case continued:
        if (nowState != TaskState.suspend && nowState != TaskState.started) {
          throw new TaskManageException("当前状态不是暂停或者运行，无法执行继续操作");
        }
        for (AbstractDataExecutor executor : executors) {
          executor.doContinue();
        }
        taskStateMap.put(taskId, TaskState.started);
        break;

      case over:
        //任何状态下都可以直接销毁
        for (AbstractDataExecutor executor : executors) {
          try {
            executor.close();
          } catch (IOException e) {
            log.error("调用pipline close方法发生异常，准备强制结束线程", e);
            try {
              executor.stop();
            } catch (Throwable throwable) {
              log.error("强行结束线程发生异常", e);
            }
          }
        }
        //保留状态
        taskStateMap.put(taskId, TaskState.over);
        //移除允许再次创建
        piplineMap.remove(taskId);
        executorMap.remove(taskId);
        break;
    }
    //更新task状态
    taskMap.get(taskId).setTaskState(taskState);
  }

  @Override
  public void pauseAndStart(String taskId) {
    TaskState nowState = this.taskStateMap.get(taskId);
    if (nowState != TaskState.created) {
      throw new TaskManageException("必须是初始状态才能执行该方法！");
    }
    List<AbstractDataExecutor> executors = executorMap.get(taskId);
    if (executors == null || executors.size() == 0) {
      throw new TaskManageException("没有该任务");
    }
    for (AbstractDataExecutor executor : executors) {
      executor.pause();
      executor.start();
    }
    taskStateMap.put(taskId, TaskState.suspend);
  }

  @Override
  public void editTaskNum(String taskId, Integer numberOfPipline, SingleTask newSingleTask) {
    List<AbstractDataExecutor> abstractDataExecutors = executorMap.get(taskId);
    if (abstractDataExecutors.size() == numberOfPipline) {
      log.info("任务数相等无需修改");
      return;
    } else if (abstractDataExecutors.size() > numberOfPipline) {
      //调小
      int change = abstractDataExecutors.size() - numberOfPipline;
      changeSmall(taskId, change, abstractDataExecutors);
    } else {
      //调大，比较复杂，要关注状态！
      int change = numberOfPipline - abstractDataExecutors.size();
      changeLarge(taskId, newSingleTask, change);
    }
    taskMap.get(taskId).setNumberOfPipline(numberOfPipline);
  }

  /**
   * 调大
   *
   * @param taskId
   * @param newSingleTask
   * @param change
   */
  private void changeLarge(String taskId, SingleTask newSingleTask, int change) {
    int beigin = executorMap.get(taskId).size();
    List<IDataPipline> dataPiplineChanges = new ArrayList<>();
    List<AbstractDataExecutor> executorChanges = new ArrayList<>();
    IExecutorListener executorListener = executorListenerMap.get(newSingleTask.getTaskId());
    try {
      IDataSinkCreator dataSinkCreator = getDataSinkCreator(newSingleTask);
      doCreatePipline(executorMap.get(taskId).size(), change, newSingleTask, dataSinkCreator, dataPiplineChanges, executorChanges, executorListener);
      piplineMap.get(taskId).addAll(dataPiplineChanges);
      executorMap.get(taskId).addAll(executorChanges);
      executorListener.onPiplineCreate(beigin, change, null);
    } catch (Throwable throwable) {
      executorListener.onPiplineCreate(beigin, change, throwable);
      throw throwable;
    }
    //获取当前状态
    TaskState taskState = taskStateMap.get(taskId);
    switch (taskState) {
      case created:
        //啥也不用做
        break;
      case started:
        //启动
        for (AbstractDataExecutor executor : executorChanges) {
          executor.start();
        }
        break;
      case suspend:
        for (AbstractDataExecutor executor : executorChanges) {
          executor.pause();
          executor.start();
        }
        break;
      case continued:
        for (AbstractDataExecutor executor : executorChanges) {
          executor.doContinue();
          executor.start();
        }
        break;
      case over:
        //不应该执行到这里，destory状态的task不能修改
        log.error("不应该执行到这里，destory状态的task不能修改:{}", newSingleTask);
        break;
    }
  }

  /**
   * 线程数调小
   *
   * @param taskId
   * @param change
   * @param abstractDataExecutors
   */
  private void changeSmall(String taskId, Integer change, List<AbstractDataExecutor> abstractDataExecutors) {
    List<IDataPipline> dataPiplines = piplineMap.get(taskId);
    //逆序删除
    ListIterator<AbstractDataExecutor> iterator = abstractDataExecutors.listIterator(abstractDataExecutors.size());
    int count = 1;
    while (iterator.hasPrevious()) {
      if (count > change)
        break;
      AbstractDataExecutor executor = iterator.previous();
      //关闭
      try {
        executor.close();
      } catch (IOException e) {
        log.error("调整pipline数时，优雅关闭执行器失败！", e);
        try {
          executor.stop();
        } catch (Throwable throwable) {
          log.error("强行结束线程发生异常", e);
        }
      } finally {
        dataPiplines.remove(executor.getDataPipline());
        iterator.remove();
        count++;
      }
    }
  }


}
