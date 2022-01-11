package mydataharbor.web.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.rpc.util.JsonUtil;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.exception.ReconfigException;
import mydataharbor.web.service.INodeService;
import mydataharbor.web.service.IPluginRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 插件存储库，代理
 * Created by xulang on 2021/8/25.
 */
@Component
@Slf4j
public class PluginRepositoryProxy implements IPluginRepository, InitializingBean {

  @Autowired
  private LocalPluginRepository localPluginReporsitory;

  @Autowired
  private MyDataHarborPluginRepository myDataHarborPluginReporsitory;

  @Autowired
  private Map<String, IPluginRepository> pluginReporsitoryMap;

  @Autowired
  private INodeService nodeService;

  private IPluginRepository pluginReporsitory;

  private static final List<RepositoryConfig> DEFAULT_REPOSITORY_CONFIGS = new ArrayList<>();

  public PluginRepositoryProxy() {

  }

  private List<RepositoryConfig> getLatestRepoConfig() throws IOException {
    String path = Constant.NODE_PREFIX + "/" + Constant.CONFIG_FILE_PATH + "/" + Constant.PLUGIN_REPOSITORY_CONFIG_FILE_NAME;
    try {
      Stat stat = nodeService.getClient().checkExists().forPath(path);
      if (stat == null) {
        nodeService.getClient().create().creatingParentContainersIfNeeded().forPath(path);
        nodeService.getClient().setData().forPath(path, JsonUtil.serialize(DEFAULT_REPOSITORY_CONFIGS));
      }
      byte[] bytes = nodeService.getClient().getData().forPath(path);
      if (bytes.length != 0) {
        String repoConfig = new String(bytes);
        if (StringUtils.isNotBlank(repoConfig)) {
          return JsonUtil.jsonToObjectList(repoConfig, List.class, RepositoryConfig.class);
        }
      } else {
        return DEFAULT_REPOSITORY_CONFIGS;
      }
    } catch (Exception e) {
      log.error("获取repo配置文件失败", e);
    }
    throw new RuntimeException("无法读取repo.json配置文件！");
  }

  public void setProxyInstance(IPluginRepository pluginReporsitory) {
    this.pluginReporsitory = pluginReporsitory;
  }

  @Override
  public String name() {
    return pluginReporsitory.name();
  }

  @Override
  public Map<String, List<PluginGroup>> listPluginGroup() {
    return pluginReporsitory.listPluginGroup();
  }

  @Override
  public RepoPlugin query(String pluginId, String pluginVersion) {
    return pluginReporsitory.query(pluginId, pluginVersion);
  }

  @Override
  public boolean isAuth(String pluginId, String version) {
    return pluginReporsitory.isAuth(pluginId, version);
  }

  @Override
  public AuthResponse auth(String pluginId, String version) {
    return pluginReporsitory.auth(pluginId, version);
  }

  @Override
  public InputStream fetchPlugin(String pluginId, String version) throws NoAuthException, IOException {
    return pluginReporsitory.fetchPlugin(pluginId, version);
  }

  @Override
  public void upload(String fileName, String pluginId, String version, InputStream inputStream) throws IOException {
    pluginReporsitory.upload(fileName, pluginId, version, inputStream);
  }

  @Override
  public IPluginRepository next() {
    return pluginReporsitory.next();
  }

  /**
   * 重新配置
   */
  public void reConfig(List<RepositoryConfig> repositoryConfigs) throws ReconfigException {
    List<IPluginRepository> toCommitList = new ArrayList<>();
    for (int i = 0; i < repositoryConfigs.size(); i++) {
      IPluginRepository abstractPluginReporsitory = pluginReporsitoryMap.get(repositoryConfigs.get(i).getRepoName());
      if (abstractPluginReporsitory == null) {
        throw new RuntimeException("没有这个仓库：" + repositoryConfigs.get(i).getRepoName());
      }
      abstractPluginReporsitory.config(repositoryConfigs.get(i).config);
      toCommitList.add(abstractPluginReporsitory);
    }
    pluginReporsitory = toCommitList.get(0);
    if (toCommitList.get(toCommitList.size() - 1) != myDataHarborPluginReporsitory) {
      toCommitList.add(myDataHarborPluginReporsitory);
    }
    for (int i = 0; i < toCommitList.size(); i++) {
      if (pluginReporsitory instanceof AbstractPluginRepository && (i + 1) < toCommitList.size()) {
        ((AbstractPluginRepository) pluginReporsitory).setNext(toCommitList.get(i + 1));
      }
    }
  }

  @Override
  public void config(Map<String, Object> config) {

  }

  public String getNameByBean(IPluginRepository pluginReporsitory) {
    for (Map.Entry<String, IPluginRepository> stringIPluginRepositoryEntry : pluginReporsitoryMap.entrySet()) {
      if (stringIPluginRepositoryEntry.getValue() == pluginReporsitory) {
        return stringIPluginRepositoryEntry.getKey();
      }
    }
    return null;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    DEFAULT_REPOSITORY_CONFIGS.add(new RepositoryConfig(getNameByBean(localPluginReporsitory), new HashMap<>()));
    DEFAULT_REPOSITORY_CONFIGS.add(new RepositoryConfig(getNameByBean(myDataHarborPluginReporsitory), new HashMap<String, Object>() {
      {
        put("email", "");
        put("token", "");
      }
    }));
    try {
      List<RepositoryConfig> latestRepoConfig = getLatestRepoConfig();
      reConfig(latestRepoConfig);
    } catch (Exception e) {
      log.error("使用repo.json初始化仓库服务失败，将使用默认仓库配置", e);
      reConfig(DEFAULT_REPOSITORY_CONFIGS);
    }
    NodeCache nodeCache = new NodeCache(nodeService.getClient(), Constant.NODE_PREFIX + "/" + Constant.CONFIG_FILE_PATH + "/" + Constant.PLUGIN_REPOSITORY_CONFIG_FILE_NAME);
    nodeCache.getListenable().addListener(new NodeCacheListener() {
      @Override
      public void nodeChanged() throws Exception {
        try {
          List<RepositoryConfig> latestRepoConfig = getLatestRepoConfig();
          reConfig(latestRepoConfig);
        } catch (Exception e) {
          log.error("使用repo.json初始化仓库服务失败，将使用默认仓库配置", e);
          reConfig(DEFAULT_REPOSITORY_CONFIGS);
        }
      }
    });
    nodeCache.start();
  }


  public void downloadPluginToLocal(String pluginId, String version, String repoType) {
    IPluginRepository pluginRepository = pluginReporsitoryMap.get(repoType);
    if (pluginRepository == null) {
      throw new RuntimeException("没有这个插件仓库类型:" + repoType);
    }
    RepoPlugin repoPlugin = pluginRepository.query(pluginId, version);
    if (repoPlugin == null) {
      throw new RuntimeException("改插件仓库没有这个插件或者这个版本的插件");
    }
    pluginRepository.downloadToLocal(repoPlugin);
  }

  /**
   * 仓库配置
   */
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class RepositoryConfig {
    private String repoName;
    private Map<String, Object> config;
  }
}
