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


package mydataharbor.plugin.app.pluginserver;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.IPluginInfoManager;
import mydataharbor.plugin.api.IPluginRemoteManager;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.api.IRebalance;
import mydataharbor.plugin.api.ITaskManager;
import mydataharbor.plugin.api.exception.PluginServerCreateException;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginServerConfig;
import mydataharbor.plugin.app.listener.GroupChangeListener;
import mydataharbor.plugin.app.listener.GroupNodeChildrenChangeListener;
import mydataharbor.plugin.app.listener.NodeLeaderLatchListener;
import mydataharbor.plugin.app.plugin.PluginInfoManager;
import mydataharbor.plugin.app.rebalance.CommonRebalance;
import mydataharbor.plugin.app.rpc.RemoteManagerImpl;
import mydataharbor.plugin.app.task.TaskManager;
import mydataharbor.rpc.server.IRpcServer;
import mydataharbor.rpc.server.NettyRpcServer;
import mydataharbor.rpc.util.JsonUtil;
import mydataharbor.util.VersionUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.pf4j.DefaultPluginManager;
import org.pf4j.DefaultVersionManager;
import org.pf4j.PluginManager;
import org.pf4j.VersionManager;
import org.yaml.snakeyaml.Yaml;


/**
 * @auth xulang
 * @Date 2021/6/11
 **/
@Slf4j
public class PluginServerImpl implements IPluginServer {

  private Yaml yaml;

  /**
   * 服务配置
   */
  private PluginServerConfig pluginServerConfig;

  /**
   * 节点信息
   */
  private NodeInfo nodeInfo;

  /**
   * zk客户端
   */
  private CuratorFramework client;

  /**
   * 是否停止
   */
  private volatile boolean stop;

  private Thread awaitThread;

  private IPluginRemoteManager pluginRemoteManager;

  private PluginManager pluginManager;

  private IRpcServer rpcServer;

  private IPluginInfoManager pluginInfoManager;

  private ITaskManager taskManager;

  private IRebalance rebalance;

  public PluginServerImpl(String runJarPath) {
    File file = new File(runJarPath);
    if (file.isFile()) {
      runJarPath = file.getParent() + "/";
    }
    this.yaml = new Yaml();
    try {
      this.pluginServerConfig = yaml.loadAs(new FileInputStream(runJarPath + "/" + Constant.CONFIG_FILE_PATH + "/" + Constant.CONFIG_FILE_NAME), PluginServerConfig.class);
      resolveSystemConfig(pluginServerConfig);
      this.nodeInfo = new NodeInfo(pluginServerConfig);
      this.nodeInfo.setVersion(VersionUtil.getVersion());
    } catch (FileNotFoundException e) {
      log.error("pluginserver创建失败：无法读取配置文件", e);
      throw new PluginServerCreateException("pluginserver创建失败：无法读取配置文件", e);
    }
    nodeInfo.setRunJarPath(runJarPath);
    String path =  nodeInfo.getRunJarPath() + Constant.PLUGIN_PATH_WORKER;
    log.info("plugins path: {}", path);
    pluginManager = new DefaultPluginManager(Paths.get(new File(path).getAbsolutePath())){
        @Override
        protected VersionManager createVersionManager() {
            return new DefaultVersionManager(){
                @Override
                public boolean checkVersionConstraint(String version, String constraint) {
                    try {
                       return super.checkVersionConstraint(version, constraint);
                    }catch (Exception e){
                        return true;
                    }
                }
            };
        }
    };
    this.pluginInfoManager = new PluginInfoManager(this);
    this.rebalance = new CommonRebalance();
  }

  @Override
  public void start() {
    initPluginFramework();
    initRpcServer();
    initRemoteManager();
    if (pluginServerConfig.getZk() != null ) {
      RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 30);
      client = CuratorFrameworkFactory.newClient(pluginServerConfig.getZk(), retryPolicy);
      client.start();
      this.taskManager = new TaskManager(pluginInfoManager, client, this);
      initZkNodeInfo();
      initWatcher();
    }
  }

    /**
     * 解析系统配置
     *
     * @param pluginServerConfig
     */
    private void resolveSystemConfig(PluginServerConfig pluginServerConfig) {
        String group = System.getProperty("group");
        if (group != null) {
            pluginServerConfig.setGroup(group);
        }
        String nodeName = System.getProperty("nodeName");
        if (nodeName != null) {
            pluginServerConfig.setNodeName(nodeName);
        }
        String ip = System.getProperty("ip");
        if (ip != null) {
            pluginServerConfig.setIp(ip);
        }
        if (System.getProperty("port") != null) {
            Integer port = Integer.valueOf(System.getProperty("port"));
            if (port != null) {
                pluginServerConfig.setPort(port);
            }
        }
        String zk = System.getProperty("zk");
        if (zk != null) {
            pluginServerConfig.setZk(zk);
        }
        String pluginRepository = System.getProperty("pluginRepository");
        if (pluginRepository != null) {
            pluginServerConfig.setPluginRepository(pluginRepository);
        }
    }

  private void initRpcServer() {
    this.rpcServer = new NettyRpcServer(nodeInfo.getIp() + ":" + nodeInfo.getPort());
    try {
      rpcServer.start();
    } catch (InterruptedException e) {
      throw new PluginServerCreateException("启动rpc server异常！", e);
    }
  }


  private void initWatcher() {
    String lockPath = Constant.NODE_PREFIX + "/" + Constant.LEADER + "/" + nodeInfo.getGroup();
    LeaderLatch leaderLatch = new LeaderLatch(client, lockPath);
    LeaderLatchListener listener = new NodeLeaderLatchListener(nodeInfo, client);
    leaderLatch.addListener(listener);
    try {
      Stat stat = client.checkExists().forPath(lockPath);
      if (stat == null) {
        client.create().creatingParentsIfNeeded().forPath(lockPath);
      }
      leaderLatch.start();
    } catch (Exception e) {
      throw new PluginServerCreateException("启动leader监听器失败！", e);
    }

    String groupPath = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + nodeInfo.getGroup();
    //监听group节点
    NodeCache nodeCache = new NodeCache(client, groupPath);
    try {
      nodeCache.start();
      GroupChangeListener groupChangeListener = new GroupChangeListener(nodeCache, pluginRemoteManager, taskManager, this, client);
      nodeCache.getListenable().addListener(groupChangeListener);
    } catch (Exception e) {
      throw new PluginServerCreateException("启动父节点监听异常！", e);
    }
    GroupNodeChildrenChangeListener groupNodeChildrenChangeListener = new GroupNodeChildrenChangeListener(nodeInfo, rebalance);
    //监听group node子节点
    try {
      PathChildrenCache pathChildrenCache = new PathChildrenCache(client, groupPath, true);
      pathChildrenCache.getListenable().addListener(groupNodeChildrenChangeListener);
      pathChildrenCache.start();
    } catch (Exception e) {
      throw new PluginServerCreateException("启动同组内节点变化监听器失败！", e);
    }

  }


  private void initPluginFramework() {
    pluginManager.loadPlugins();
    pluginManager.startPlugins();
  }

  private void initRemoteManager() {
    this.pluginRemoteManager = new RemoteManagerImpl(this, pluginInfoManager, taskManager);
    rpcServer.addService(IPluginRemoteManager.class.getName(), "1.0", pluginRemoteManager);
  }

  @Override
  public void stop() {
    //各种close
    client.close();
    stop = true;
    pluginManager.stopPlugins();
    taskManager.close();
    rpcServer.stop();
    log.info("jvm退出，关闭实例");
  }

  @Override
  public void startDaemonAwaitThread() {
    this.awaitThread = new AwaitThread("awaitThread");
    awaitThread.setContextClassLoader(this.getClass().getClassLoader());
    awaitThread.setDaemon(false);
    awaitThread.start();
  }

  @Override
  public PluginServerConfig getPluginServerConfig() {
    return pluginServerConfig;
  }

  @Override
  public PluginManager getPluginManager() {
    return pluginManager;
  }

  @Override
  public NodeInfo getNodeInfo() {
    return nodeInfo;
  }

  public void initZkNodeInfo() {
    String path = generateRegistPath();
    try {
      Stat exist = client.checkExists().forPath(path);
      while (exist != null) {
        log.warn("节点:{}已经存在！更换节点名称重试...", path);
        nodeInfo.rename();
        path = generateRegistPath();
        exist = client.checkExists().forPath(path);
      }
    } catch (Exception e) {
      throw new PluginServerCreateException("初始化zk失败！", e);
    }
    String nodeInfoJson = JsonUtil.objectToJson(nodeInfo);
    try {
      client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, nodeInfoJson.getBytes());
    } catch (Exception e) {
      log.error("向zk注册节点发生异常", e);
      throw new PluginServerCreateException("向zk注册节点发生异常");
    }
  }


  /**
   * 生成zk注册地址
   *
   * @return
   */
  public String generateRegistPath() {
    return Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/" + nodeInfo.getGroup() + "/" + nodeInfo.getNodeName();
  }

  class AwaitThread extends Thread {

    public AwaitThread(String name) {
      super(name);
    }

    public void run() {
      while (!stop) {
        try {
          Thread.sleep(10000);
        } catch (InterruptedException ex) {
          // continue and check the flag
        }
      }
    }
  }


}
