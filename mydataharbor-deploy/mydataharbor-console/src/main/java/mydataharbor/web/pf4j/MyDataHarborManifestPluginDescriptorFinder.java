package mydataharbor.web.pf4j;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;

public class MyDataHarborManifestPluginDescriptorFinder extends ManifestPluginDescriptorFinder {

  private static final String PLUGIN_GROUP = "Plugin-Group";
  private static final String PLUGIN_GROUP_LOGO = "Plugin-Group-Logo";
  private static final String MY_DATA_HARBOR_VERSION = "Mydataharbor-Version";
  private static final String PLUGIN_UPDATE_INFO = "Plugin-Update-Info";

  @Override
  protected PluginDescriptor createPluginDescriptor(Manifest manifest) {
    PluginDescriptor pluginDescriptor = super.createPluginDescriptor(manifest);
    if (pluginDescriptor instanceof MyDataHarborPluginDescriptor) {
      Attributes mainAttributes = manifest.getMainAttributes();
      MyDataHarborPluginDescriptor myDataHarborPluginDescriptor = (MyDataHarborPluginDescriptor) pluginDescriptor;
      myDataHarborPluginDescriptor.setPluginGroup(mainAttributes.getValue(PLUGIN_GROUP));
      myDataHarborPluginDescriptor.setPluginGroupLogo(mainAttributes.getValue(PLUGIN_GROUP_LOGO));
      myDataHarborPluginDescriptor.setMydataharborVersion(mainAttributes.getValue(MY_DATA_HARBOR_VERSION));
      myDataHarborPluginDescriptor.setPluginUpdateInfo(mainAttributes.getValue(PLUGIN_UPDATE_INFO));
    }
    return pluginDescriptor;
  }

  @Override
  protected DefaultPluginDescriptor createPluginDescriptorInstance() {
    return new MyDataHarborPluginDescriptor();
  }
}