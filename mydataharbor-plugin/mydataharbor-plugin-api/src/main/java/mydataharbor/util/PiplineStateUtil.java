package mydataharbor.util;

import mydataharbor.pipline.PiplineState;
import mydataharbor.plugin.api.task.TaskAssignedInfo;

import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/7/2
 **/
public class PiplineStateUtil {

  public static boolean piplineIsDone(PiplineState piplineState) {
    if (piplineState == PiplineState.crash_done
      || piplineState == PiplineState.success_done
      || piplineState == PiplineState.schedule_done) {
      return true;
    }
    return false;
  }


  public static boolean doesAssignedTaskAllCreatedSuccess(TaskAssignedInfo taskAssignedInfo) {
    Map<String, TaskAssignedInfo.NodeAssignedInfo> assignedInfoMap = taskAssignedInfo.getAssignedInfoMap();
    for (Map.Entry<String, TaskAssignedInfo.NodeAssignedInfo> stringNodeAssignedInfoEntry : assignedInfoMap.entrySet()) {
      TaskAssignedInfo.NodeAssignedInfo assignedInfo = stringNodeAssignedInfoEntry.getValue();
      if (assignedInfo.isDiverted()) {
        continue;
      }
      Map<String, PiplineState> piplineStates = assignedInfo.getPiplineStates();
      if (piplineStates != null) {
        for (Map.Entry<String, PiplineState> stringPiplineStateEntry : piplineStates.entrySet()) {
          if (stringPiplineStateEntry.getValue() == PiplineState.create_error) {
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
      Map<String, PiplineState> piplineStates = nodeAssignedInfo.getPiplineStates();
      if (piplineStates != null) {
        for (Map.Entry<String, PiplineState> stringPiplineStateEntry : piplineStates.entrySet()) {
          if (!piplineIsDone(stringPiplineStateEntry.getValue())) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
