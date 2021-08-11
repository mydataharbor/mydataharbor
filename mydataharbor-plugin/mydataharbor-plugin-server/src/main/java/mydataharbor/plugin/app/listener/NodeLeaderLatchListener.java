package mydataharbor.plugin.app.listener;

import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.node.NodeInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import mydataharbor.rpc.util.JsonUtil;

import java.util.function.Consumer;

/**
 * @auth xulang
 * @Date 2021/7/3
 **/
@Slf4j
public class NodeLeaderLatchListener implements LeaderLatchListener {

  private NodeInfo nodeInfo;
  private CuratorFramework client;

  public NodeLeaderLatchListener(NodeInfo nodeInfo, CuratorFramework client) {
    this.nodeInfo = nodeInfo;
    this.client = client;
  }

  public void nodeInfoChange(Consumer<NodeInfo> consumer) {
    if (client != null) {
      while (true) {
        try {
          String nodePath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + nodeInfo.getGroup() + "/" + nodeInfo.getNodeName();
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(nodePath);
          NodeInfo nodeInfo = JsonUtil.deserialize(bytes, NodeInfo.class);
          consumer.accept(nodeInfo);
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

  @Override
  public synchronized void isLeader() {
    if (!nodeInfo.isLeader()) {
      log.info("i become leader");
      nodeInfo.setLeader(true);
      nodeInfoChange(nodeInfo -> nodeInfo.setLeader(true));
    }
  }

  @Override
  public synchronized void notLeader() {
    if (nodeInfo.isLeader()) {
      log.info("im not leader any more!");
      nodeInfo.setLeader(false);
      nodeInfoChange(nodeInfo -> nodeInfo.setLeader(false));
    }
  }
}
