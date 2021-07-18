package mydataharbor.web.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import mydataharbor.web.entity.PluginEntity;
import org.pf4j.PluginDescriptor;

/**
 * @auth xulang
 * @Date 2021/6/28
 **/
public interface IPluginService {

  /**
   * 上传插件
   *
   * @param fileName
   * @param filePath
   * @param pluginDescriptor
   * @return
   */
  PluginEntity uploadPlugin(String fileName, String filePath, PluginDescriptor pluginDescriptor);

  /**
   * 精确查询
   *
   * @param pluginId
   * @param version
   * @return
   */
  PluginEntity query(String pluginId, String version);

  /**
   * 模糊分页查询
   *
   * @param pluginId
   * @return
   */
  IPage<PluginEntity> pageQuery(IPage<PluginEntity> page, String pluginId, String version, String des);
}
