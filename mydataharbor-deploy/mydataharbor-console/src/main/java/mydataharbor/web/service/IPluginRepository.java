package mydataharbor.web.service;

import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 插件存储库
 * Created by xulang on 2021/8/25.
 */
public interface IPluginRepository {

  /**
   * 存储库名称
   *
   * @return
   */
  String name();

  /**
   * 列出存储库支持的所有插件
   *
   * @return
   */
  List<PluginGroup> listPluginGroup();

  /**
   * 是否存在
   *
   * @param pluginId
   * @param pluginVersion
   * @return
   */
  RepoPlugin query(String pluginId, String pluginVersion);

  /**
   * 当前用户对某个插件某个版本是否有权限
   *
   * @param pluginId
   * @param version
   * @return
   */
  boolean isAuth(String pluginId, String version);

  /**
   * 对当前用户授权
   *
   * @param pluginId
   * @param version
   * @return
   */
  AuthResponse auth(String pluginId, String version);

  /**
   * 下载
   *
   * @param pluginId
   * @param version
   * @return
   */
  InputStream fetchPlugin(String pluginId, String version) throws NoAuthException, IOException;


  /**
   * 上传插件
   *
   * @param fileName
   * @param inputStream
   * @return
   */
  void upload(String fileName, String pluginId, String version, InputStream inputStream) throws IOException;

  /**
   * 获取下一个存储库
   *
   * @return
   */
  IPluginRepository next();

  /**
   * 配置
   *
   * @param config
   */
  void config(Map<String, Object> config);
}
