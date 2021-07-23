package mydataharbor.web.controller;

import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.web.base.BaseResponse;
import mydataharbor.web.entity.TaskEditRequest;
import mydataharbor.web.service.ITaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
@Api(tags = "task")
@RestController
@RequestMapping("/mydataharbor/task/")
public class TaskController {

  @Autowired
  private ITaskService taskService;

  @PostMapping("submit")
  @ApiOperation("向集群提交任务")
  @ResponseBody
  public BaseResponse<TaskAssignedInfo> submitTask(@ApiParam("任务信息") @RequestBody DistributedTask distributedTask) {
    distributedTask.setTaskState(TaskState.created);
    return BaseResponse.success(taskService.submitTask(distributedTask));
  }


  @PostMapping("manageTaskState")
  @ApiOperation("管理任务状态")
  @ResponseBody
  public BaseResponse<Boolean> manageTaskState(@ApiParam("taskId") @RequestParam("taskId") String taskId, @ApiParam("taskState") @RequestParam("taskState") TaskState taskState) {
    taskService.manageTaskState(taskId, taskState);
    return BaseResponse.success(true);
  }


  @PostMapping("listTask")
  @ApiOperation("列出集群任务")
  @ResponseBody
  public BaseResponse<Map<String, DistributedTask>> listTask() {
    return BaseResponse.success(taskService.listTasks());
  }

  @PostMapping("editTask")
  @ApiOperation("修改任务")
  @ResponseBody
  public BaseResponse<Boolean> editTask(@ApiParam("修改请求参数") @RequestBody TaskEditRequest taskEditRequest) {
    return BaseResponse.success(taskService.editTask(taskEditRequest));
  }
}
