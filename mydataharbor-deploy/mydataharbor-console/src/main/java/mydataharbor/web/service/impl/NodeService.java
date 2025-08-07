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


package mydataharbor.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.IPluginRemoteManager;
import mydataharbor.plugin.api.exception.PluginLoadException;
import mydataharbor.plugin.api.exception.UninstallPluginException;
import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.rpc.client.RpcClient;
import mydataharbor.rpc.util.JsonUtil;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.service.IGroupChangeAction;
import mydataharbor.web.service.INodeService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @auth xulang
 * @Date 2021/6/23
 **/
@Component
@Slf4j
public class NodeService implements INodeService, InitializingBean {

  @Value("${zk}")
  private List<String> zk;

  /**
   * zk客户端
   */
  private CuratorFramework client;

  private Map<String, IPluginRemoteManager> rpcPluginServerMap = new ConcurrentHashMap<>();

  private volatile Map<String, NodeInfo> nodeInfoCache = new ConcurrentHashMap<>();

  private volatile Map<String, GroupInfo> groupInfoCache = new ConcurrentHashMap<>();

  @Override
  public Map<String, List<NodeInfo>> lisNode() {
    Map<String, List<NodeInfo>> nodeInfoMap = new ConcurrentHashMap<>();
    Set<Map.Entry<String, NodeInfo>> entries = nodeInfoCache.entrySet();
    for (Map.Entry<String, NodeInfo> entry : entries) {
      List<NodeInfo> nodeInfos = nodeInfoMap.get(entry.getValue().getGroup());
      if (nodeInfos == null) {
        nodeInfos = new ArrayList<>();
        nodeInfoMap.put(entry.getValue().getGroup(), nodeInfos);
      }
      nodeInfos.add(entry.getValue());
    }
    return nodeInfoMap;
  }

  @Override
  public Map<String, GroupInfo> listGroupInfo() {
    return groupInfoCache;
  }

  @Override
  public void groupTouch(String groupName, IGroupChangeAction groupChangeAction) {
    //查询group信息
    String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + groupName;
    try {
      while (true) {
        try {
          Stat stat = new Stat();
          byte[] data = client.getData().storingStatIn(stat).forPath(groupPath);
          if (groupChangeAction != null) {
            GroupInfo groupInfo = JsonUtil.deserialize(data, GroupInfo.class);
            groupChangeAction.action(groupInfo);
            client.setData().withVersion(stat.getVersion()).forPath(groupPath, JsonUtil.serialize(groupInfo));
          } else {
            client.setData().withVersion(stat.getVersion()).forPath(groupPath, data);
          }
          break;
        } catch (KeeperException.BadVersionException e) {
          log.warn("乐观锁重试", e);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("zk操作失败！", e);
    }
  }

  @Override
  public void deleteGroup(String groupName) {
    GroupInfo groupInfo = groupInfoCache.get(groupName);
    if (groupInfo != null && (!groupInfo.getTasks().isEmpty() || !groupInfo.getNodeInfos().isEmpty())) {
      throw new RuntimeException("该分组还有任务没有删除，或者还有节点还在运行，请检查");
    }
    //查询group信息
    String groupLockPath = Constant.LOCK_PATH + Constant.NODE_NAME + "_" + groupName;
    InterProcessLock lock = new InterProcessMutex(client, groupLockPath);
    try {
      lock.acquire();
      //删除
      String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + groupName;
      client.delete().forPath(groupPath);
      lock.release();
    } catch (Exception e) {
      throw new RuntimeException("删除group：" + groupName + "失败：" + e.getMessage(), e);
    }
  }

  /**
   * 通过nodeName 获得rpc代理
   *
   * @param nodeName
   * @return
   */
  public IPluginRemoteManager getRpcPluginServerByNodeName(String nodeName) {
    NodeInfo nodeInfo = nodeInfoCache.get(nodeName);
    if (nodeInfo == null) {
      throw new RuntimeException("没有该机器注册！");
    }
    IPluginRemoteManager rpcPluginServer = rpcPluginServerMap.get(nodeName);
    if (rpcPluginServer == null) {
      synchronized (NodeService.class) {
        rpcPluginServer = rpcPluginServerMap.get(nodeName);
        if (rpcPluginServer == null) {
          try {
            rpcPluginServer = RpcClient.createService(IPluginRemoteManager.class, nodeInfo.getIp(), nodeInfo.getPort() + "");
          } catch (Exception e) {
            throw new RuntimeException("创建rpc服务出错！", e);
          }
        }
        rpcPluginServerMap.put(nodeName, rpcPluginServer);
      }
    }
    return rpcPluginServer;
  }

  @Override
  public List<PluginInfo> getPluginInfoByGroupName(String groupName) {
    GroupInfo groupInfo = groupInfoCache.get(groupName);
    if (groupInfo == null) {
      throw new RuntimeException("该分组不存在！");
    }
    NodeInfo[] nodeInfos = groupInfo.getNodeInfos().toArray(new NodeInfo[groupInfo.getNodeInfos().size()]);
    List<PluginInfo> installedPlugins = groupInfo.getInstalledPlugins();
    if (nodeInfos == null || nodeInfos.length == 0) {
      return installedPlugins;
    }
    //每次请求随机选取
    int index = RandomUtils.nextInt(0, nodeInfos.length);
    for (PluginInfo installedPlugin : installedPlugins) {
      IPluginRemoteManager pluginRemoteManager = getRpcPluginServerByNodeName(nodeInfos[index].getNodeName());
      PluginInfo pluginInfo = pluginRemoteManager.getPluginInfoByPluginId(installedPlugin.getPluginId());
      if (pluginInfo == null) {
        //插件安装失败
        continue;
      }
      installedPlugin.setDataPipelineCreatorInfos(pluginInfo.getDataPipelineCreatorInfos());
    }
    return installedPlugins;
  }

  /**
   * 获取groupinfo
   *
   * @return
   */
  public Map<String, GroupInfo> fetchGroupInfo() {
    Map<String, GroupInfo> groupInfos = new ConcurrentHashMap<>();
    try {
      String path = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME;
      Stat stat = client.checkExists().forPath(path);
      if (stat == null) {
        log.warn("没有机器注册！");
        return Collections.emptyMap();
      }
      List<String> groupChildren = client.getChildren().forPath(path);
      for (String groupChild : groupChildren) {
        String groupPath = path + "/" + groupChild;
        byte[] bytes = client.getData().forPath(groupPath);
        if (bytes != null && bytes.length > 0) {
          GroupInfo groupInfo = JsonUtil.deserialize(bytes, GroupInfo.class);
          groupInfos.put(groupInfo.getGroupName(), groupInfo);
        } else {
          GroupInfo groupInfo = new GroupInfo();
          groupInfo.setGroupName(groupChild);
          groupInfos.put(groupChild, groupInfo);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("通过zk获取节点列表失败！", e);
    }
    return groupInfos;
  }

  /**
   * 获取节点信息
   *
   * @return
   */
  public Map<String, NodeInfo> fetchNodeInfo() {
    Map<String, NodeInfo> nodeInfos = new ConcurrentHashMap<>();
    try {
      String path = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME;
      Stat stat = client.checkExists().forPath(path);
      if (stat == null) {
        log.warn("没有机器注册！");
        return Collections.emptyMap();
      }
      List<String> groupChildren = client.getChildren().forPath(path);
      for (String groupChild : groupChildren) {
        String groupPath = path + "/" + groupChild;
        List<String> nodeChildren = client.getChildren().forPath(groupPath);
        for (String nodeChild : nodeChildren) {
          String childPath = groupPath + "/" + nodeChild;
          byte[] bytes = client.getData().forPath(childPath);
          NodeInfo nodeInfo = JsonUtil.deserialize(bytes, NodeInfo.class);
          nodeInfos.put(nodeInfo.getNodeName(), nodeInfo);
          GroupInfo groupInfo = groupInfoCache.get(nodeInfo.getGroup());
          if (groupInfo != null) {
            groupInfo.getNodeInfos().add(nodeInfo);
          }
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("通过zk获取节点列表失败！", e);
    }
    return nodeInfos;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 30);
    String zkAddress = StringUtils.join(zk, ",");
    client = CuratorFrameworkFactory.newClient(zkAddress, retryPolicy);
    client.start();
    this.groupInfoCache = fetchGroupInfo();
    this.nodeInfoCache = fetchNodeInfo();
    String path = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME;
    TreeCache treeCache = new TreeCache(client, path);
    treeCache.getListenable().addListener((client, event) -> {
      this.groupInfoCache = fetchGroupInfo();
      this.nodeInfoCache = fetchNodeInfo();
    });
    treeCache.start();
  }

  /**
   * 安装插件
   */
  @Override
  public PluginInfo installPluginByRpcUpload(String fileName, RepoPlugin repoPluginDescriptor, byte[] body, String groupName) {
    //查询group信息
    String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + groupName;
    try {
      List<String> groupNodes = client.getChildren().forPath(groupPath);
      if (groupNodes.size() == 0) {
        //还没有节点启动
        throw new PluginLoadException("还没有节点启动");
      }
      //并行安装
      groupNodes.parallelStream().forEach(nodeName -> {
        try {
          String pluginId = getRpcPluginServerByNodeName(nodeName).loadPluginByRpc(fileName, body);
          getRpcPluginServerByNodeName(nodeName).startPlugin(pluginId);
        } catch (Exception e) {
          log.error("插件安装中有某些机器失败！组名:{},机器号：{}", groupName, nodeName);
        }
      });
    } catch (Exception e) {
      throw new PluginLoadException("error:" + e.getMessage(), e);
    }
    installPluginByReporsitory(repoPluginDescriptor.getPluginId(),repoPluginDescriptor.getVersion(),repoPluginDescriptor,groupName);
    return repoPluginDescriptor;
  }

  @Override
  public PluginInfo installPluginByReporsitory(String pluginId, String version, RepoPlugin repoPlugin, String groupName) {
    //查询group信息
    String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + groupName;
    GroupInfo groupInfo;
    PluginInfo pluginInfo;
    try {
      while (true) {
        groupInfo = new GroupInfo();
        pluginInfo = repoPlugin;
        try {
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(groupPath);
          if (bytes != null && bytes.length > 0) {
            groupInfo = JsonUtil.deserialize(bytes, GroupInfo.class);
          } else {
            groupInfo.setGroupName(groupName);
          }
          //判断插件是否已经安装
          List<PluginInfo> installedPlugins = groupInfo.getInstalledPlugins();
          boolean updatePluginVersion = false;
          for (PluginInfo installedPlugin : installedPlugins) {
            if (installedPlugin.getPluginId().equals(pluginInfo.getPluginId())) {
              if(installedPlugin.getVersion().equals(pluginInfo.getVersion())) {
                  throw new RuntimeException(pluginInfo.getPluginId() + "该插件已经安装！安装信息：" + installedPlugin.toString());
              } else {
                  updatePluginVersion = true;
                  installedPlugin.setVersion(pluginInfo.getVersion());
              }
            }
          }
          if(!updatePluginVersion)
            groupInfo.getInstalledPlugins().add(pluginInfo);
          String groupInfoJson = JsonUtil.objectToJson(groupInfo);
          client.setData().withVersion(stat.getVersion()).forPath(groupPath, groupInfoJson.getBytes());
          break;
        } catch (KeeperException.BadVersionException e) {
          log.warn("乐观锁生效", e);
        }
      }
    } catch (Exception e) {
      log.error("error！");
      throw new PluginLoadException("error:" + e.getMessage(), e);
    }
    return pluginInfo;
  }

  public IPluginRemoteManager getRpcPluginServer(String nodeName) {
    return getRpcPluginServerByNodeName(nodeName);
  }

  @Override
  public boolean uninstallPlugin(String pluginId, String groupName) {
    //查询group信息
    String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + groupName;

    try {
      while (true) {
        try {
          Stat stat = new Stat();
          byte[] bytes = client.getData().storingStatIn(stat).forPath(groupPath);
          GroupInfo groupInfo = null;
          if (bytes != null && bytes.length > 0) {
            groupInfo = JsonUtil.deserialize(bytes, GroupInfo.class);
          }
          if (groupInfo == null) {
            throw new UninstallPluginException("没有该groupName:" + groupName);
          }
          Iterator<PluginInfo> iterator = groupInfo.getInstalledPlugins().iterator();
          while (iterator.hasNext()) {
            PluginInfo pluginInfo = iterator.next();
            if (pluginInfo.getPluginId().equals(pluginId)) {
              iterator.remove();
            }
          }
          String groupInfoJson = JsonUtil.objectToJson(groupInfo);
          client.setData().withVersion(stat.getVersion()).forPath(groupPath, groupInfoJson.getBytes());
          break;
        } catch (KeeperException.BadVersionException e) {
          log.warn("乐观锁生效,重试...", e);
        }
      }
    } catch (Exception e) {
      log.error("error！");
      throw new PluginLoadException("error:" + e.getMessage(), e);
    }
    return true;
  }

  @Override
  public CuratorFramework getClient() {
    return client;
  }
}