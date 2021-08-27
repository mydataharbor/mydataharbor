package mydataharbor.web.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件分组
 * Created by xulang on 2021/8/25.
 */
@Data
public class PluginGroup {
  /**
   * 分组名称
   */
  private String groupName;

  /**
   * 分组图片 base64
   */
  private String imageBase64;

  /**
   * 仓库名称
   */
  private String repoName;

  /**
   * 仓库类型
   */
  private String repoType;

  /**
   *
   */
  private List<PluginId> plugins = new ArrayList<>();
}
