package mydataharbor.monitor;

import lombok.extern.slf4j.Slf4j;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @auth xulang
 * @Date 2021/7/15
 **/
@Slf4j
public class Taskmonitor implements TaskmonitorMBean {

  private String taskId;

  private AtomicLong tRecordCount = new AtomicLong();

  private AtomicLong protocalConventSuccessCount = new AtomicLong();

  private AtomicLong protocalConventErrorCount = new AtomicLong();

  private AtomicLong checkerSuccessCount = new AtomicLong();

  private AtomicLong checkerErrorCount = new AtomicLong();

  private AtomicLong dataConventSuccessCount = new AtomicLong();

  private AtomicLong dataConventErrorCount = new AtomicLong();

  private AtomicLong writeSuccessCount = new AtomicLong();

  private AtomicLong writeErrorCount = new AtomicLong();

  private Long lastRunTime;

  private static final Map<String, Taskmonitor> TASKMONITOR_CACHE = new ConcurrentHashMap<>();

  public static Taskmonitor getTaskmonitorByExecutorId(String executorId) {
    return TASKMONITOR_CACHE.get(executorId);
  }

  public Taskmonitor(String taskId, String executorId) {
    this.taskId = taskId;
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();

    try {
      ObjectName mbeanName = new ObjectName("mydataharbor.task.count:name=" + executorId);
      boolean isMBeanRegistered = server.isRegistered(mbeanName);
      if (isMBeanRegistered) {
        log.info("Unregistering existing JMX MBean [{}].", mbeanName);
        try {
          server.unregisterMBean(mbeanName);
        } catch (InstanceNotFoundException e) {
          log.info("JMX MBean not found to unregister [{}].", mbeanName);
        }
      }
      server.registerMBean(this, mbeanName);
    } catch (Exception e) {
      log.error("注册jmx bean失败", e);
    }

  }


  @Override
  public String getTaskId() {
    return taskId;
  }

  @Override
  public Long getTRecordCount() {
    return tRecordCount.get();
  }

  @Override
  public Long getProtocalConventSuccessCount() {
    return protocalConventSuccessCount.get();
  }

  @Override
  public Long getProtocalConventErrorCount() {
    return protocalConventErrorCount.get();
  }

  @Override
  public Long getCheckerSuccessCount() {
    return checkerSuccessCount.get();
  }

  @Override
  public Long getCheckerErrorCount() {
    return checkerErrorCount.get();
  }

  @Override
  public Long getDataConventSuccessCount() {
    return dataConventSuccessCount.get();
  }

  @Override
  public Long getDataConventErrorCount() {
    return dataConventErrorCount.get();
  }

  @Override
  public Long getWriteSuccessCount() {
    return writeSuccessCount.get();
  }

  @Override
  public Long getWriteErrorCount() {
    return writeErrorCount.get();
  }

  @Override
  public Long getLastRunTime() {
    return lastRunTime;
  }

  public void addAndGettRecordCount(Long change) {
    tRecordCount.addAndGet(change);
  }

  public void addAndGetProtocalConventSuccessCount(Long change) {
    protocalConventSuccessCount.addAndGet(change);
  }

  public void addAndGetProtocalConventErrorCount(Long change) {
    protocalConventErrorCount.addAndGet(change);
  }

  public void addAndGetCheckerSuccessCount(Long change) {
    checkerSuccessCount.addAndGet(change);
  }

  public void addAndGetCheckerErrorCount(Long change) {
    checkerErrorCount.addAndGet(change);
  }

  public void addAndGetDataConventSuccessCount(Long change) {
    dataConventSuccessCount.addAndGet(change);
  }

  public void addAndGetDataConventErrorCount(Long change) {
    dataConventErrorCount.addAndGet(change);
  }

  public void addAndGetWriteSuccessCount(Long change) {
    writeSuccessCount.addAndGet(change);
  }

  public void addAndGetWriteErrorCount(Long change) {
    writeErrorCount.addAndGet(change);
  }

  @Override
  public void setLastRunTime(Long lastRunTime) {
    this.lastRunTime = lastRunTime;
  }
}
