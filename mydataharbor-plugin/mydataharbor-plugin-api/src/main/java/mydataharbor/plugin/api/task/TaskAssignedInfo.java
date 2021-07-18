package mydataharbor.plugin.api.task;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/

import mydataharbor.pipline.PiplineState;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 任务分配信息
 */
@Data
public class TaskAssignedInfo {

  private String taskId;

  /**
   * key是节点名称
   */
  private Map<String, NodeAssignedInfo> assignedInfoMap = new ConcurrentHashMap<>();

  @Data
  public static class NodeAssignedInfo {
    /**
     * 是否已经转移
     */
    boolean diverted = false;
    /**
     * 节点名称
     */
    private String nodeName;
    /**
     * 任务数
     */
    private Integer taskNum = 0;

    /**
     * 每个pipline运行状态，key是线程名
     */
    private Map<String, PiplineState> piplineStates = new HashMap<>();

    /**
     * 每一个pipline写入数据
     */
    private Map<String, Long> writeTotal = new HashMap<>();

    /**
     * 创建过程中的异常
     */
    private String createException;

    public Integer addAndGetTaskNum(Integer add) {
      taskNum += add;
      return taskNum;
    }
  }
}
