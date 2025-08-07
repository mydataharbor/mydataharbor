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


package mydataharbor.plugin.api.group;

import lombok.Data;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集群分组信息
 *
 * @auth xulang
 * @Date 2021/6/25
 **/
@Data
public class GroupInfo {

  public GroupInfo() {
    this.installedPlugins = new ArrayList<>();
    this.nodeInfos = new HashSet<>();
    this.tasks = new ArrayList<>();
  }

  /**
   * 分组名称
   */
  private String groupName;

  /**
   * 节点信息
   */
  private Set<NodeInfo> nodeInfos;

  /**
   * 安装的插件信息
   */
  private List<PluginInfo> installedPlugins;

  /**
   * 任务id列表
   */
  private List<String> tasks;
}