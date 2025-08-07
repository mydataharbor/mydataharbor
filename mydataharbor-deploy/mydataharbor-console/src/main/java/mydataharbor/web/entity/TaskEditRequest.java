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


package mydataharbor.web.entity;

import lombok.Data;

/**
 * 修改任务请求
 * 任务只能修改以下几个参数
 * @auth xulang
 * @Date 2021/7/7
 **/
@Data
public class TaskEditRequest {
  /**
   * 任务id
   */
  private String taskId;

  /**
   * 故障转移
   */
  private Boolean enableRebalance;

    /**
     * 负载均衡
     */
    private Boolean enableLoadBalance;

  /**
   * pipeline条数
   */
  private Integer totalNumberOfPipeline;

  /**
   * 任务名称
   */
  private String taskName;

}