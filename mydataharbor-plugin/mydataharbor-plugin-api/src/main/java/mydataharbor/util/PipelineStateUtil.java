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


package mydataharbor.util;

import mydataharbor.pipeline.PipelineState;
import mydataharbor.plugin.api.task.TaskAssignedInfo;

import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/7/2
 **/
public class PipelineStateUtil {

  public static boolean pipelineIsDone(PipelineState pipelineState) {
      return pipelineState == PipelineState.crash_done
              || pipelineState == PipelineState.success_done
              || pipelineState == PipelineState.schedule_done
              || pipelineState == PipelineState.create_error;
  }


  public static boolean doesAssignedTaskAllCreatedSuccess(TaskAssignedInfo taskAssignedInfo) {
    Map<String, TaskAssignedInfo.NodeAssignedInfo> assignedInfoMap = taskAssignedInfo.getAssignedInfoMap();
    for (Map.Entry<String, TaskAssignedInfo.NodeAssignedInfo> stringNodeAssignedInfoEntry : assignedInfoMap.entrySet()) {
      TaskAssignedInfo.NodeAssignedInfo assignedInfo = stringNodeAssignedInfoEntry.getValue();
      if (assignedInfo.isDiverted()) {
        continue;
      }
      Map<String, PipelineState> pipelineStates = assignedInfo.getPipelineStates();
      if (pipelineStates != null) {
        for (Map.Entry<String, PipelineState> stringPipelineStateEntry : pipelineStates.entrySet()) {
          if (stringPipelineStateEntry.getValue() == PipelineState.create_error) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static boolean doesAssignedTaskAllDone(TaskAssignedInfo taskAssignedInfo) {
    Map<String, TaskAssignedInfo.NodeAssignedInfo> assignedInfoMap = taskAssignedInfo.getAssignedInfoMap();
    for (Map.Entry<String, TaskAssignedInfo.NodeAssignedInfo> stringNodeAssignedInfoEntry : assignedInfoMap.entrySet()) {
      TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = stringNodeAssignedInfoEntry.getValue();
      if (nodeAssignedInfo.isDiverted()) {
        continue;
      }
      Map<String, PipelineState> pipelineStates = nodeAssignedInfo.getPipelineStates();
      if (pipelineStates != null) {
        for (Map.Entry<String, PipelineState> stringPipelineStateEntry : pipelineStates.entrySet()) {
          if (!pipelineIsDone(stringPipelineStateEntry.getValue())) {
            return false;
          }
        }
      }
    }
    return true;
  }
}