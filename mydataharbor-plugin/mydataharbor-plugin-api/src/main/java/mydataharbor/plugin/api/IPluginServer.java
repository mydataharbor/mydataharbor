package mydataharbor.plugin.api;

import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginServerConfig;
import org.pf4j.PluginManager;

/**
 * plugin server服务
 *
 * @auth xulang
 * @Date 2021/6/11
 **/
public interface IPluginServer {
  /**
   * 启动服务
   */
  void start();

  /**
   * 关闭服务
   */
  void stop();


  /**
   * 保持jvm不退出
   */
  void startDaemonAwaitThread();

  /**
   * 获得配置
   *
   * @return
   */
  PluginServerConfig getPluginServerConfig();

  /**
   * 获得插件管理器
   *
   * @return
   */
  PluginManager getPluginManager();

  /**
   * 获取节点信息
   *
   * @return
   */
  NodeInfo getNodeInfo();


}
