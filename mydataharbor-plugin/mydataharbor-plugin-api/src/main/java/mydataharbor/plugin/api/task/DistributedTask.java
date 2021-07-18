package mydataharbor.plugin.api.task;

import mydataharbor.constant.Constant;
import mydataharbor.util.RandomStringUtil;
import lombok.Data;

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
   * 是否支持再平衡
   * 如果为true，同一个组中的机器增加或者减少，都会停止所有的任务，并且重新分配，然后启动
   * 如果设置为false，只有当运行任务的机器挂了并且changeNodeWhenCrash为true，才会把属于这台机器的任务分配到其它机器
   */
  private boolean enableRebalance = true;


  /**
   * pipline条数
   */
  private Integer totalNumberOfPipline = 1;
  /**
   * 任务分配信息
   */
  private TaskAssignedInfo taskAssignedInfo;

  public String generateTaskId() {
    String taskId = Constant.TASK_PATH + "-" + getGroupName() + "-" + RandomStringUtil.generateRandomStr(10);
    return taskId;
  }

}
