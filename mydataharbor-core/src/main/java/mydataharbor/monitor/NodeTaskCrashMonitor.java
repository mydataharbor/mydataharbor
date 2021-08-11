package mydataharbor.monitor;

import lombok.extern.slf4j.Slf4j;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/7/16
 **/
@Slf4j
public class NodeTaskCrashMonitor implements NodeTaskCrashMonitorMBean {

  private String taskId;

  private String nodeName;

  private String nodeIp;

  private Integer nodePort;

  private Integer taskNum;

  public NodeTaskCrashMonitor(String taskId, String nodeName, String nodeIp, Integer nodePort, Integer taskNum, Map<String, String> otherInfo) {
    this.taskId = taskId;
    this.nodeName = nodeName;
    this.nodeIp = nodeIp;
    this.nodePort = nodePort;
    this.taskNum = taskNum;
    Hashtable property = new Hashtable();
    property.put("ametric", "node-crash-task");
    property.put("taskId", taskId);
    property.put("nodeIp", nodeIp);
    property.put("nodePort", nodePort);
    property.put("nodeName", nodeName);
    if (otherInfo != null) {
      property.putAll(otherInfo);
    }
    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
    try {
      ObjectName mbeanName = new ObjectName("mydataharbor", property);
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
