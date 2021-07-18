package mydataharbor.monitor;

import lombok.extern.slf4j.Slf4j;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * @auth xulang
 * @Date 2021/7/16
 **/
@Slf4j
public class ExecutorCrash implements ExecutorCrashMBean {

  private String taskId;

  private String nodeName;

  private String nodeIp;

  private Integer nodePort;

  private Integer taskNum;

  public ExecutorCrash(String taskId, String nodeName, String nodeIp, Integer nodePort, Integer taskNum) {
    this.taskId = taskId;
    this.nodeName = nodeName;
    this.nodeIp = nodeIp;
    this.nodePort = nodePort;
    this.taskNum = taskNum;
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName mbeanName = new ObjectName("mydataharbor.task.crash:name=" + taskId + "@" + nodeName);
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
  public String getNodeName() {
    return nodeName;
  }

  @Override
  public String getNodeIp() {
    return nodeIp;
  }

  @Override
  public Integer getNodePort() {
    return nodePort;
  }

  @Override
  public Integer getTaskNum() {
    return taskNum;
  }


}
