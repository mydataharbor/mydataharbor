package mydataharbor.plugin.app.listener;

import mydataharbor.IDataPipline;
import mydataharbor.IExecutorListener;
import mydataharbor.constant.Constant;
import mydataharbor.executor.AbstractDataExecutor;
import mydataharbor.pipline.PiplineState;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;
import mydataharbor.plugin.api.task.TaskState;
import mydataharbor.util.PiplineStateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import mydataharbor.rpc.util.JsonUtil;

/**
 * @auth xulang
 * @Date 2021/7/2
 **/
@Slf4j
public class ExecutorListener implements IExecutorListener {

  private String taskId;

  private CuratorFramework client;

  private NodeInfo nodeInfo;

  public ExecutorListener(String taskId, CuratorFramework client, NodeInfo nodeInfo) {
    this.taskId = taskId;
    this.client = client;
    this.nodeInfo = nodeInfo;
  }

  @Override
  public void onPiplineCreate(int begin, int change, Throwable throwable) {
    if (client != null) {
      //记录下创建异常
      while (true) {
        try {
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(Constant.TASK_PATH_PARENT + taskId);
          DistributedTask distributedTask = JsonUtil.deserialize(bytes, DistributedTask.class);
          TaskAssignedInfo taskAssignedInfo = distributedTask.getTaskAssignedInfo();
          TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(nodeInfo.getNodeName());
          if (throwable != null) {
            //创建失败
            nodeAssignedInfo.setCreateException(throwable.getMessage());
            for (int i = begin; i < change + begin; i++) {
              nodeAssignedInfo.getPiplineStates().put(taskId + "-" + (i + 1), PiplineState.create_error);
            }
          } else {
            //创建成功
            for (int i = begin; i < change + begin; i++) {
              nodeAssignedInfo.getPiplineStates().put(taskId + "-" + (i + 1), PiplineState.create);
            }
          }
          client.setData().withVersion(stat.getVersion()).forPath(Constant.TASK_PATH_PARENT + taskId, JsonUtil.serialize(distributedTask));
          break;
        } catch (KeeperException.BadVersionException badVersionException) {
          log.warn("乐观锁生效，重试..");
        } catch (Exception exception) {
          log.error("更新状态发生异常！", exception);
          break;
        }
      }
      if (throwable == null)
        nodeTaskNumChange(Long.valueOf(change));
    }
  }

  @Override
  public void onRun(AbstractDataExecutor executor, IDataPipline pipline) {
    updateExecutorState(executor, PiplineState.running, null);
  }

  public void nodeTaskNumChange(Long change) {
    if (client != null) {
      while (true) {
        try {
          String nodePath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + nodeInfo.getGroup() + "/" + nodeInfo.getNodeName();
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(nodePath);
          NodeInfo nodeInfo = JsonUtil.deserialize(bytes, NodeInfo.class);
          nodeInfo.getTaskNum().addAndGet(change);
          client.setData().withVersion(stat.getVersion()).forPath(nodePath, JsonUtil.serialize(nodeInfo));
          break;
        } catch (KeeperException.BadVersionException badVersionException) {
          log.warn("乐观锁生效，重试..");
        } catch (Exception e) {
          log.error("更新状态发生异常！", e);
          break;
        }
      }
    }
  }

  private void updateExecutorState(AbstractDataExecutor executor, PiplineState piplineState, Long writeTotal) {
    if (client != null) {
      //记录下创建异常
      while (true) {
        try {
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(Constant.TASK_PATH_PARENT + taskId);
          DistributedTask distributedTask = JsonUtil.deserialize(bytes, DistributedTask.class);
          TaskAssignedInfo taskAssignedInfo = distributedTask.getTaskAssignedInfo();
          TaskAssignedInfo.NodeAssignedInfo nodeAssignedInfo = taskAssignedInfo.getAssignedInfoMap().get(nodeInfo.getNodeName());
          nodeAssignedInfo.getPiplineStates().put(executor.getName(), piplineState);
          if (writeTotal != null) {
            nodeAssignedInfo.getWriteTotal().put(executor.getName(), writeTotal);
          }
          if (PiplineStateUtil.piplineIsDone(piplineState)) {
            //将任务整体状态职位over
            if (PiplineStateUtil.doesAssignedTaskAllDone(taskAssignedInfo)) {
              distributedTask.setTaskState(TaskState.over);
            }
          }
          client.setData().withVersion(stat.getVersion()).forPath(Constant.TASK_PATH_PARENT + taskId, JsonUtil.serialize(distributedTask));
          break;
        } catch (KeeperException.BadVersionException badVersionException) {
          log.warn("乐观锁生效，重试..");
        } catch (Exception exception) {
          log.error("更新状态发生异常！", exception);
          break;
        }
      }
    }
  }

  @Override
  public void onSuspend(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal) {
    updateExecutorState(executor, PiplineState.suspend, writeTotal);
  }

  @Override
  public void onContinue(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal) {
    updateExecutorState(executor, PiplineState.running, writeTotal);
  }

  @Override
  public void onSucccessEnd(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal, boolean isRun) {
    if (isRun) {
      updateExecutorState(executor, PiplineState.success_done, writeTotal);
    } else {
      updateExecutorState(executor, PiplineState.schedule_done, writeTotal);
    }
  }

  @Override
  public void onClose(AbstractDataExecutor executor, IDataPipline pipline, long writeTotal, boolean isRun) {
    nodeTaskNumChange(-1L);
  }

  @Override
  public void onExceptionEnd(AbstractDataExecutor executor, IDataPipline pipline, Throwable throwable, long writeTotal) {
    updateExecutorState(executor, PiplineState.crash_done, writeTotal);
  }
}
