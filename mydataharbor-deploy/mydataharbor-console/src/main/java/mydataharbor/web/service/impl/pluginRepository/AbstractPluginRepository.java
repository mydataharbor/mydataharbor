package mydataharbor.web.service.impl.pluginRepository;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.service.IPluginRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xulang on 2021/8/26.
 */
@Slf4j
public abstract class AbstractPluginRepository implements IPluginRepository {

  private IPluginRepository next;

  public AbstractPluginRepository(IPluginRepository next) {
    this.next = next;
  }

  public void setNext(IPluginRepository next) {
    this.next = next;
  }

  @Override
  public Map<String, List<PluginGroup>> listPluginGroup() {
    Map<String, List<PluginGroup>> plugins = new HashMap<>();
    List<PluginGroup> pluginGroups = doListPluginGroup();
    pluginGroups = pluginGroups == null ? new ArrayList<>() : pluginGroups;
    plugins.put(name(), pluginGroups);
    if (next() != null) {
      plugins.putAll(next().listPluginGroup());
    }
    return plugins;
  }

  /**
   * 列出当前存储库支持的所有插件
   *
   * @return
   */
  abstract List<PluginGroup> doListPluginGroup();

  /**
   * 真正下载
   *
   * @param pluginId
   * @param version
   * @return
   * @throws NoAuthException
   * @throws FileNotFoundException
   */
  abstract InputStream doFetchPlugin(String pluginId, String version) throws NoAuthException, IOException;

  @Override
  public IPluginRepository next() {
    return next;
  }


  @Override
  public InputStream fetchPlugin(String pluginId, String version) throws NoAuthException, IOException {
    try {
      if (!isAuth(pluginId, version)) {
        AuthResponse authResponse = auth(pluginId, version);
        if (!authResponse.isSuccess()) {
          log.warn("存储器:{},授权{}@{}，失败，原因：{}！", name(), pluginId, version, authResponse);
          throw new NoAuthException("对存储器" + name() + "尝试授权失败，失败信息:" + authResponse.getMsg());
        }
      }
      return doFetchPlugin(pluginId, version);
    } catch (NoAuthException | IOException e) {
      throw e;
    }
  }

  @Override
  public void config(Map<String, Object> config) {

  }
}
