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


package mydataharbor.plugin.api.task;

import lombok.Data;
import mydataharbor.constant.Constant;
import mydataharbor.util.RandomStringUtil;

/**
 * 分布式情况下的task元信息
 *
 * @auth xulang
 * @Date 2021/6/30
 **/
@Data
public class DistributedTask extends Task {

  /**
   * 分布式组
   */
  private String groupName;

    /**
     * 是否支持故障转移
     * 值为false，节点停机，任务将不会自动转移！但是会有告警信息发出，适合不能重复运行的任务
     * 值为true，当节点异常停机的时候，任务将会自动转移到其他节点运行
     */
  private boolean enableRebalance = true;

    /**
     * 负载均衡
     * 当集群内有节点加入是，负载均衡的任务有可能会被重新分配到其他节点（任务会被有中断的过程），以便分散压力，如果不希望任务随意被停止转移请设置为false
     */
  private boolean enableLoadBalance = true;


  /**
   * pipeline条数
   */
  private Integer totalNumberOfPipeline = 1;
  /**
   * 任务分配信息
   */
  private TaskAssignedInfo taskAssignedInfo;

  /**
   * 创建时间
   */
  private long createTime = System.currentTimeMillis();

  /**
   * 修改时间
   */
  private long lastUpdateTime = System.currentTimeMillis();

  public String generateTaskId() {
    String taskId = Constant.TASK_PATH + "-" + getGroupName() + "-" + RandomStringUtil.generateRandomStr(10);
    return taskId;
  }

}