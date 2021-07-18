package mydataharbor.plugin.api.group;

import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import lombok.Data;

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
