package mydataharbor.web.service;

import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.IPluginRemoteManager;
import org.apache.curator.framework.CuratorFramework;
import org.pf4j.PluginDescriptor;

import java.util.List;
import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/6/23
 **/
public interface INodeService {
  /**
   * 列出集群内所有jvm节点
   *
   * @return
   */
  Map<String, List<NodeInfo>> lisNode();

  /**
   * 列出group
   *
   * @return
   */
  Map<String, GroupInfo> listGroupInfo();

  /**
   * 触发group的变更通知
   *
   * @param groupName
   */
  void groupTouch(String groupName, IGroupChangeAction groupChangeAction);


  /**
   * 通过组名称获得插件详细信息，会触发对node的调用
   *
   * @param groupName
   * @return
   */
  List<PluginInfo> getPluginInfoByGroupName(String groupName);

  /**
   * 通过rpc安装插件
   *
   * @param fileName
   * @param body
   * @param nodeGroup 集群分组
   * @return
   */
  PluginInfo installPluginByRpcUpload(String fileName, PluginDescriptor pluginDescriptor, byte[] body, String nodeGroup);

  /**
   * 通过仓库来安装
   *
   * @param pluginId
   * @param version
   * @param pluginDescriptor
   * @param nodeGroup
   * @return
   */
  PluginInfo installPluginByReporsitory(String pluginId, String version, PluginDescriptor pluginDescriptor, String nodeGroup);

  /**
   * 获取rpc实例
   *
   * @param nodeName
   * @return
   */
  IPluginRemoteManager getRpcPluginServer(String nodeName);

  /**
   * 卸载插件
   *
   * @param pluginId
   * @param nodeGroup
   * @return
   */
  boolean uninstallPlugin(String pluginId, String nodeGroup);

  /**
   * 获取zk连接客户端
   *
   * @return
   */
  CuratorFramework getClient();


}
