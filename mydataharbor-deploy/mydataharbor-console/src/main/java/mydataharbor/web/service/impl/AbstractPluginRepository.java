package mydataharbor.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.service.IPluginRepository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
  public List<PluginGroup> listPluginGroup() {
    List<PluginGroup> pluginGroups = doListPluginGroup();
    pluginGroups = pluginGroups == null ? new ArrayList<>() : pluginGroups;
    if (getNext() != null) {
      pluginGroups.addAll(getNext().listPluginGroup());
    }
    return pluginGroups;
  }

  /**
   * 列出存储库支持的所有插件
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

  public IPluginRepository getNext() {
    return next;
  }

  @Override
  public RepoPlugin query(String pluginId, String pluginVersion) {
    RepoPlugin repoPlugin = doQuery(pluginId, pluginVersion);
    if (repoPlugin != null) {
      return repoPlugin;
    } else if (getNext() != null) {
      return getNext().query(pluginId, pluginVersion);
    }
    return null;
  }

  abstract RepoPlugin doQuery(String pluginId, String pluginVersion);

  @Override
  public InputStream fetchPlugin(String pluginId, String version) throws NoAuthException, IOException {
    try {
      if (isAuth(pluginId, version)) {
        return doFetchPlugin(pluginId, version);
      } else {
        AuthResponse authResponse = auth(pluginId, version);
        if (authResponse.isSuccess()) {
          log.warn("授权{}@{}，成功！", pluginId, version);
          return doFetchPlugin(pluginId, version);
        } else {
          throw new NoAuthException("对存储器" + name() + "尝试授权失败，失败信息:" + authResponse.getMsg());
        }
      }
    } catch (NoAuthException | IOException e) {
      log.error("当前存储器{}获插件{}@{}异常，尝试使用next存储器获取", name(), pluginId, version);
      if (getNext() != null) {
        return getNext().fetchPlugin(pluginId, version);
      } else {
        log.warn("next存储器为空！");
        throw e;
      }
    }
  }

  @Override
  public void config(Map<String, Object> config) {

  }
}
