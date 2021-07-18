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
   * 是否支持再平衡
   * 如果为true，同一个组中的机器增加或者减少，都会停止所有的任务，并且重新分配，然后启动
   * 如果设置为false，只有当运行任务的机器挂了并且changeNodeWhenCrash为true，才会把属于这台机器的任务分配到其它机器
   */
  private Boolean enableRebalance;


  /**
   * pipline条数
   */
  private Integer totalNumberOfPipline;
}
