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


package mydataharbor.plugin.api;

import mydataharbor.IDataPipelineCreator;
import mydataharbor.plugin.api.plugin.PluginInfo;

import java.util.List;
import java.util.Map;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
public interface IPluginInfoManager {

  /**
   * 获取插件信息，从缓存获取
   *
   * @return
   */
  List<PluginInfo> getAllPluginInfos();

  /**
   * 刷新当前缓存信息
   *
   * @throws InstantiationException
   * @throws IllegalAccessException
   */
  void refresh() throws InstantiationException, IllegalAccessException;

  /**
   * 通过pluginid  扫描这个插件下的所有创建器,耗时
   *
   * @param pluginId
   * @return
   */
  Map<String, IDataPipelineCreator> scanDataPipelineCreatorByPlugin(String pluginId);

  /**
   * 扫描这个jvm下的所有插件及其内部信息
   *
   * @return
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  List<PluginInfo> scanAllPluginInfo() throws IllegalAccessException, InstantiationException;

}