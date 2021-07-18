package mydataharbor.plugin.api.plugin;

import lombok.Data;

import java.util.List;

/**
 * @auth xulang
 * @Date 2021/6/11
 **/
@Data
public class PluginServerConfig {

  private List<String> zk;

  /**
   * rmi 远程通信ip（用户可用指定，不指定的话自动获取）
   */
  private String ip;

  /**
   * 端口
   */
  private Integer port = 2021;

  /**
   * 节点名称，可选，系统自动命名
   */
  private String nodeName;

  /**
   * 机器分组
   */
  private String group = "default";

  /**
   * 插件仓库
   */
  private String pluginRepository;
}
