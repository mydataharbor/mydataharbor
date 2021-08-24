package mydataharbor.web.pf4j;

import org.pf4j.DefaultPluginDescriptor;

/**
 * Created by xulang on 2021/8/21.
 */
public class MyDataHarborPluginDescriptor extends DefaultPluginDescriptor {
  /**
   * 组件分组
   */
  private String pluginGroup;

  /**
   * 分组logo
   */
  private String pluginGroupLogo;

  /**
   * 编译环境信息
   */
  private String mydataharborVersion;

  public String getPluginGroup() {
    return pluginGroup;
  }

  public void setPluginGroup(String pluginGroup) {
    this.pluginGroup = pluginGroup;
  }

  public String getPluginGroupLogo() {
    return pluginGroupLogo;
  }

  public void setPluginGroupLogo(String pluginGroupLogo) {
    this.pluginGroupLogo = pluginGroupLogo;
  }

  public void setMydataharborVersion(String mydataharborVersion) {
    this.mydataharborVersion = mydataharborVersion;
  }

  public String getMydataharborVersion() {
    return mydataharborVersion;
  }
}
