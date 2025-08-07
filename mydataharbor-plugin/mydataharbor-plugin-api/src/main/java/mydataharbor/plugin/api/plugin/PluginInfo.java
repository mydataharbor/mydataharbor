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


package mydataharbor.plugin.api.plugin;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.pf4j.PluginDescriptor;

/**
 *
 */
@Data
public class PluginInfo implements Serializable {

  /**
   * 插件id
   */
  private String pluginId;

  /**
   * 版本
   */
  private String version;


  /**
   * 插件描述
   */
  private String pluginDescription;


  /**
   * 插件依赖
   */
  private List<PluginInfo.PluginDependency> dependencies;

  /**
   * 授权信息
   */
  private String license;


  private String requires;

  private String pluginClass;
  private String provider;

  /**
   * creator信息
   */
  private List<DataPipelineCreatorInfo> dataPipelineCreatorInfos;

  public void fillByPluginDescriptor(PluginDescriptor pluginDescriptor) {
    this.setPluginId(pluginDescriptor.getPluginId());
    this.setVersion(pluginDescriptor.getVersion());
    this.setLicense(pluginDescriptor.getLicense());
    this.setPluginClass(pluginDescriptor.getPluginClass());
    this.setPluginDescription(pluginDescriptor.getPluginDescription());
    this.setProvider(pluginDescriptor.getProvider());
    this.setRequires(pluginDescriptor.getRequires());
    List<org.pf4j.PluginDependency> dependencies = pluginDescriptor.getDependencies();
    List<PluginInfo.PluginDependency> pluginDependencies = new ArrayList<>();
    for (org.pf4j.PluginDependency dependency : dependencies) {
      PluginInfo.PluginDependency pluginDependency = new PluginInfo.PluginDependency();
      pluginDependency.setPluginId(dependency.getPluginId());
      pluginDependency.setOptional(dependency.isOptional());
      pluginDependency.setPluginVersionSupport(dependency.getPluginVersionSupport());
      pluginDependencies.add(pluginDependency);
    }
    this.setDependencies(pluginDependencies);
  }


  @Data
  public static class PluginDependency implements Serializable {
    private String pluginId;
    private String pluginVersionSupport = "*";
    private boolean optional;
  }

}