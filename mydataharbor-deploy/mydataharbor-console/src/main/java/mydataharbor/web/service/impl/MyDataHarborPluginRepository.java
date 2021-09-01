package mydataharbor.web.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.rpc.util.JsonUtil;
import mydataharbor.web.base.BaseResponse;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.service.IPluginRepository;
import okhttp3.*;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 官方插件仓库，链条的最后一个节点，相当于maven的中央仓库
 * Created by xulang on 2021/8/26.
 */
@Repository(MyDataHarborPluginRepository.REPO_TYPE)
@Slf4j
public class MyDataHarborPluginRepository implements IPluginRepository {

  private static final String MYDATAHARBOR_REPORSITORY_HOST = "https://www.mydataharbor.com";

  public static final String REPO_TYPE = "MyDataHarbor-Reporsitory";

  private OkHttpClient httpClient = new OkHttpClient().newBuilder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .readTimeout(600, TimeUnit.SECONDS)
    .build();

  private List<PluginGroup> pluginGroups = new ArrayList<>();

  private String email;

  private String token;

  public MyDataHarborPluginRepository() {
  }


  private String makeSign(String time, byte[] salt, Object... args) throws Exception {
    StringBuilder stringBuilder = new StringBuilder(time);
    if (args != null)
      for (Object arg : args) {
        stringBuilder.append(arg.toString());
      }

    PBEKeySpec pbeKeySpec = new PBEKeySpec(token.toCharArray());
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBEWITHMD5andDES");
    Key key = factory.generateSecret(pbeKeySpec);

    //加密
    PBEParameterSpec pbeParameterSpec = new PBEParameterSpec(salt, 100);
    Cipher cipher = Cipher.getInstance("PBEWITHMD5andDES");
    cipher.init(Cipher.ENCRYPT_MODE, key, pbeParameterSpec);
    byte[] result = cipher.doFinal(stringBuilder.toString().getBytes());
    return Base64.getEncoder().encodeToString(result);

  }

  @Override
  public String name() {
    return "MyDataHarbor官方插件存储库";
  }

  private Request createRequest(String path, RequestBody requestBody, Object... args) throws Exception {
    String time = new Date().toString();
    SecureRandom random = new SecureRandom();
    byte[] salt = random.generateSeed(8);

    return new Request.Builder()
      .addHeader("email", email)
      .addHeader("salt", java.util.Base64.getEncoder().encodeToString(salt))
      .addHeader("sign", makeSign(time, salt, args))
      .addHeader("time", time)
      .post(requestBody)
      .url(MYDATAHARBOR_REPORSITORY_HOST + path).build();
  }

  @Override
  public List<PluginGroup> listPluginGroup() {
    return pluginGroups;
  }


  /**
   * 10分钟获取一次
   */
  @Scheduled(fixedRate = 1000 * 60 * 10)
  private void scheduleFetchPluginGroups() {
    try {
      Response response = httpClient.newCall(createRequest("/repo/listPluginGroup", new FormBody(new ArrayList<>(), new ArrayList<>()))).execute();
      if (response.isSuccessful()) {
        String body = response.body().string();
        BaseResponse<List<PluginGroup>> baseResponse = JsonUtil.getObjMapper().readValue(body, new
          TypeReference<BaseResponse<List<PluginGroup>>>() {
          });
        if (baseResponse.getCode() == 0) {
          this.pluginGroups = baseResponse.getData();
        }
      }
    } catch (Exception e) {
      log.error("请求插件仓库失败！", e);
    }
  }

  @Override
  public RepoPlugin query(String pluginId, String pluginVersion) {
    try {
      Response response = httpClient.newCall(createRequest("/repo/query", new FormBody(Arrays.asList("pluginId", "version"), Arrays.asList(pluginId, pluginVersion)), pluginId, pluginVersion)).execute();
      if (response.isSuccessful()) {
        String body = response.body().string();
        BaseResponse<RepoPlugin> baseResponse = JsonUtil.getObjMapper().readValue(body, new
          TypeReference<BaseResponse<RepoPlugin>>() {
          });
        if (baseResponse.getCode() == 0) {
          return baseResponse.getData();
        } else {
          throw new NoAuthException(baseResponse.getMsg());
        }
      }
    } catch (Exception e) {
      log.error("请求插件仓库失败！", e);
    }
    return null;
  }

  @Override
  public boolean isAuth(String pluginId, String version) {
    try {
      Response response = httpClient.newCall(createRequest("/repo/isAuth", new FormBody(Arrays.asList("pluginId", "version"), Arrays.asList(pluginId, version)), pluginId, version)).execute();
      if (response.isSuccessful()) {
        String body = response.body().string();
        BaseResponse<Boolean> baseResponse = JsonUtil.getObjMapper().readValue(body, new
          TypeReference<BaseResponse<Boolean>>() {
          });
        if (baseResponse.getCode() == 0) {
          return baseResponse.getData();
        } else {
          return false;
        }
      }
    } catch (Exception e) {
      log.error("请求插件仓库失败！", e);
    }
    return false;
  }

  @Override
  public AuthResponse auth(String pluginId, String version) {
    try {
      Response response = httpClient.newCall(createRequest("/repo/auth", new FormBody(Arrays.asList("pluginId", "version"), Arrays.asList(pluginId, version)), pluginId, version)).execute();
      if (response.isSuccessful()) {
        String body = response.body().string();
        BaseResponse<AuthResponse> baseResponse = JsonUtil.getObjMapper().readValue(body, new
          TypeReference<BaseResponse<AuthResponse>>() {
          });
        return baseResponse.getData();
      }
    } catch (Exception e) {
      log.error("请求插件仓库失败！", e);
      return new AuthResponse(false, "授权失败！:" + e.getMessage());
    }
    return new AuthResponse(false, "授权失败！:");
  }

  @Override
  public InputStream fetchPlugin(String pluginId, String version) throws NoAuthException, IOException {
    try {
      Response response = httpClient.newCall(createRequest("/repo/fetchPlugin", new FormBody(Arrays.asList("pluginId", "version"), Arrays.asList(pluginId, version)), pluginId, version)).execute();
      if (response.isSuccessful()) {
        return new ByteArrayInputStream(response.body().bytes());
      } else {
        throw new NoAuthException("响应码：" + response.code() + " 响应信息:" + response.message());
      }
    } catch (Exception e) {
      log.error("请求插件仓库失败！", e);
    }
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
    scheduleFetchPluginGroups();
  }

}
