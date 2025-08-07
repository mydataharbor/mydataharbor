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


package mydataharbor.monitor;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.Hashtable;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;

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