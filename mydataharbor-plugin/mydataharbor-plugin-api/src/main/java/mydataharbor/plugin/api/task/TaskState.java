package mydataharbor.plugin.api.task;

/**
 * 任务状态
 *
 * @auth xulang
 * @Date 2021/6/23
 **/
public enum TaskState {

  /**
   * 已创建
   */
  created,

  /**
   * 暂停
   */
  suspend,

  /**
   * 继续
   */
  continued,

  /**
   * 启动
   */
  started,

  /**
   * 结束
   */
  over;
}
