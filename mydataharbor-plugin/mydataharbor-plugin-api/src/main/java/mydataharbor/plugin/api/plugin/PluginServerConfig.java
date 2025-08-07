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

import java.util.List;

/**
 * @auth xulang
 * @Date 2021/6/11
 **/
@Data
public class PluginServerConfig {

  private String zk;

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