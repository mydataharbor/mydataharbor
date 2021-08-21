package mydataharbor.web.pf4j;

import org.pf4j.DefaultPluginDescriptor;

/**
 * Created by xulang on 2021/8/21.
 */
public class MyDataHarborPluginDescriptor extends DefaultPluginDescriptor {
  /**
   * 组件分组
   */
  private String group;

  /**
   * 分组logo
   */
  private String groupLogo;

  /**
   * 编译环境信息
   */
  private String mydataharborVersion;

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getGroupLogo() {
    return groupLogo;
  }

  public void setGroupLogo(String groupLogo) {
    this.groupLogo = groupLogo;
  }

  public void setMydataharborVersion(String mydataharborVersion) {
    this.mydataharborVersion = mydataharborVersion;
  }

  public String getMydataharborVersion() {
    return mydataharborVersion;
  }
}
