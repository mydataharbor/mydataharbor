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