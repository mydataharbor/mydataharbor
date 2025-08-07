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

/**
 * @auth xulang
 * @Date 2021/6/30
 **/

import lombok.Data;
import mydataharbor.pipeline.PipelineState;

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
     * 每个pipeline运行状态，key是线程名
     */
    private Map<String, PipelineState> pipelineStates = new HashMap<>();

    /**
     * 每一个pipeline写入数据
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