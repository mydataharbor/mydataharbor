package mydataharbor.web.entity;

import lombok.Data;
import mydataharbor.plugin.api.plugin.PluginInfo;

/**
 * 插件详细信息
 * Created by xulang on 2021/8/25.
 */
@Data
public class RepoPlugin extends PluginInfo {

  /**
   * 文件名
   */
  private String fileName;


  /**
   * 插件编译时候使用的mydataharbor版本号
   */
  private String mydataharborVersion;

  /**
   * 是否已经授权
   */
  private boolean authed = true;

  /**
   * 授权说明信息,html
   */
  private String otherInfoHtml;
}
