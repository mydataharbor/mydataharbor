package mydataharbor.monitor;

/**
 * 执行器宕机，任务无法Rebalance监控
 * @auth xulang
 * @Date 2021/7/16
 **/
public interface NodeTaskCrashMonitorMBean {
  /**
   * 任务id
   * @return
   */
  String getTaskId();

  /**
   * 宕机节点名称
   * @return
   */
  String getNodeName();

  /**
   * 节点ip
   * @return
   */
  String getNodeIp();

  /**
   * 节点端口
   * @return
   */
  Integer getNodePort();

  /**
   * 宕机任务数
   * @return
   */
  Integer getTaskNum();
}
