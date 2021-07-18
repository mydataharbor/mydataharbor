
package mydataharbor.plugin.api.plugin;

import lombok.Data;
import org.pf4j.PluginDescriptor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
@Data
public class PluginInfo implements Serializable {

  private String pluginId;
  private String pluginDescription;
  private String pluginClass;
  private String version;
  private String requires = "*"; // SemVer format
  private String provider;
  private List<PluginDependency> dependencies;
  private String license;

  /**
   * creator信息
   */
  private List<DataSinkCreatorInfo> dataSinkCreatorInfos;


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
