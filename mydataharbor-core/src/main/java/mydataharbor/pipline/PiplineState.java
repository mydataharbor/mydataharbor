package mydataharbor.pipline;

/**
 * 状态
 *
 * @auth xulang
 * @Date 2021/7/1
 **/
public enum PiplineState {
  /**
   * 创建
   */
  create,

  /**
   * 创建失败
   */
  create_error,

  /**
   * 暂停
   */
  suspend,

  /**
   * 正在运行
   */
  running,

  /**
   * 成功完成
   */
  success_done,

  /**
   * 异常退出
   */
  crash_done,

  /**
   * 调度完成，比如缩容,关闭等
   */
  schedule_done
}
