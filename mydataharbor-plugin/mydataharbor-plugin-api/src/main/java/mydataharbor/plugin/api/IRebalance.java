package mydataharbor.plugin.api;

import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.task.DistributedTask;
import mydataharbor.plugin.api.task.TaskAssignedInfo;

import java.util.List;

/**
 * Rebalance接口
 *
 * @auth xulang
 * @Date 2021/7/3
 **/
public interface IRebalance {

  /**
   * Rebalance算法接口
   *
   * @param liveNodes
   * @param distributedTask
   * @return
   */
  TaskAssignedInfo rebalance(List<NodeInfo> liveNodes, DistributedTask distributedTask, NodeInfo changeNode, boolean join);

}
