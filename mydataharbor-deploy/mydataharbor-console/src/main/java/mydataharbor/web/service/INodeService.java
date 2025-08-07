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


package mydataharbor.web.service;

import mydataharbor.plugin.api.IPluginRemoteManager;
import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.web.entity.RepoPlugin;

import java.util.List;
import java.util.Map;

import org.apache.curator.framework.CuratorFramework;

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
   * 删除group
   *
   * @param groupName
   */
  void deleteGroup(String groupName);


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
  PluginInfo installPluginByRpcUpload(String fileName, RepoPlugin repoPluginDescriptor, byte[] body, String nodeGroup);

  /**
   * 通过仓库来安装
   *
   * @param pluginId
   * @param version
   * @param repoPluginDescriptor
   * @param nodeGroup
   * @return
   */
  PluginInfo installPluginByReporsitory(String pluginId, String version, RepoPlugin repoPluginDescriptor, String nodeGroup);

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