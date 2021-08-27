package mydataharbor.web.service.impl;

import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.service.IPluginRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 官方插件仓库，链条的最后一个节点，相当于maven的中央仓库
 * Created by xulang on 2021/8/26.
 */
@Repository(MyDataHarborPluginRepository.REPO_TYPE)
public class MyDataHarborPluginRepository implements IPluginRepository {

  private static final String MYDATAHARBOR_REPORSITORY_HOST = "https://repo.mydataharbor.com";

  public static final String REPO_TYPE = "MyDataHarbor-Reporsitory";

  private String email;

  private String token;

  public MyDataHarborPluginRepository() {
  }

  @Override
  public String name() {
    return "MyDataHarbor官方插件存储库";
  }

  @Override
  public List<PluginGroup> listPluginGroup() {
    return new ArrayList<>();
  }

  @Override
  public RepoPlugin query(String pluginId, String pluginVersion) {
    return null;
  }

  @Override
  public boolean isAuth(String pluginId, String version) {
    return false;
  }

  @Override
  public AuthResponse auth(String pluginId, String version) {
    return new AuthResponse(true, "授权成功！");
  }

  @Override
  public InputStream fetchPlugin(String pluginId, String version) throws NoAuthException, IOException {
    return null;
  }


  @Override
  public void upload(String fileName, String pluginId, String version, InputStream inputStream) throws IOException {
    throw new RuntimeException("云仓库无法通过此方式上传插件！");
  }

  @Override
  public IPluginRepository next() {
    return null;
  }

  @Override
  public void config(Map<String, Object> config) {
    this.email = (String) config.get("email");
    this.token = (String) config.get("token");
  }
}
