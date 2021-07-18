package mydataharbor.plugin.api;

import mydataharbor.IDataSinkCreator;
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
  Map<String, IDataSinkCreator> getDataSinkCreatorMapByPlugin(String pluginId);

  /**
   * 扫描这个jvm下的所有插件及其内部信息
   *
   * @return
   * @throws IllegalAccessException
   * @throws InstantiationException
   */
  List<PluginInfo> scanAllPluginInfo() throws IllegalAccessException, InstantiationException;

}
