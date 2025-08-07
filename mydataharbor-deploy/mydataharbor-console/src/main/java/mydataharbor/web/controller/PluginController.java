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


package mydataharbor.web.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import mydataharbor.web.base.BaseResponse;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.service.impl.pluginRepository.PluginRepositoryProxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @auth xulang
 * @Date 2021/6/27
 **/
@Api(tags = "plugin")
@RestController
@RequestMapping("/mydataharbor/plugin/")
public class PluginController {

  @Autowired
  private CompoundPluginDescriptorFinder compoundPluginDescriptorFinder;

  @Autowired
  private PluginRepositoryProxy pluginReporsitory;

  @RequestMapping(value = "/uploadPlugin", method = {RequestMethod.POST}, headers = "content-type=multipart/form-data")
  @ResponseBody
  @ApiOperation("上传插件")
  public BaseResponse<PluginDescriptor> uploadPlugin(@ApiParam(value = "文件", required = true) @RequestPart("file") MultipartFile file) {
    if (file.isEmpty()) {
      throw new RuntimeException("上传失败，请选择文件");
    }
    try {
      String fileName = file.getOriginalFilename();
      Path tempFile = Files.createTempFile(null, ".jar");
      file.transferTo(tempFile);
      PluginDescriptor pluginDescriptor = compoundPluginDescriptorFinder.find(tempFile);
      if (StringUtils.isBlank(pluginDescriptor.getPluginId()) || StringUtils.isBlank(pluginDescriptor.getVersion())) {
        throw new RuntimeException("插件不合法！");
      }
      pluginReporsitory.upload(fileName, pluginDescriptor.getPluginId(), pluginDescriptor.getVersion(), new FileInputStream(tempFile.toFile()));
      return BaseResponse.success(pluginDescriptor);
    } catch (Exception e) {
      throw new RuntimeException("上传插件异常！:" + e.getMessage(), e);
    }
  }

  @GetMapping("/listPlugins")
  @ApiOperation("列出所有的插件")
  public BaseResponse<Map<String, List<PluginGroup>>> listPlugins() {
    return BaseResponse.success(pluginReporsitory.listPluginGroup());
  }

  @GetMapping("/downloadPluginToLocal")
  @ApiOperation("下载插件到本地")
  public BaseResponse<Boolean> downloadPluginToLocal(@RequestParam("pluginId") String pluginId, @RequestParam("version") String version, @RequestParam("repoType") String repoType) {
    pluginReporsitory.downloadPluginToLocal(pluginId, version, repoType);
    return BaseResponse.success(true);
  }


  @GetMapping("downloadPlugin")
  @ApiOperation("下载插件")
  public ResponseEntity<InputStreamResource> downloadPlugin(@RequestParam("pluginId") String pluginId, @RequestParam("version") String version)
    throws IOException, NoAuthException {
    try {
      RepoPlugin repoPlugin = pluginReporsitory.query(pluginId, version);
      if (repoPlugin == null) {
        return ResponseEntity.notFound().build();
      }
      InputStream inputStream = pluginReporsitory.fetchPlugin(pluginId, version);
      if (inputStream == null) {
        return ResponseEntity.notFound().build();
      }
      HttpHeaders headers = new HttpHeaders();
      headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
      headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", repoPlugin.getFileName()));
      headers.add("Pragma", "no-cache");
      headers.add("Expires", "0");
      return ResponseEntity
        .ok()
        .headers(headers)
        .contentLength(inputStream.available())
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(new InputStreamResource(inputStream));
    } catch (Exception e) {
      return ResponseEntity.notFound().build();
    }

  }

  @GetMapping("queryPluginRepoConfig")
  @ApiOperation("查询插件仓库配置")
  public BaseResponse<Map<String, PluginRepositoryProxy.RepositoryConfig>> queryPluginRepoConfig() throws IOException {
    Map<String, PluginRepositoryProxy.RepositoryConfig> repositoryConfigMap = new HashMap<>();
    List<PluginRepositoryProxy.RepositoryConfig> repoConfig = pluginReporsitory.getLatestRepoConfig();
    for (PluginRepositoryProxy.RepositoryConfig repositoryConfig : repoConfig) {
      repositoryConfigMap.put(repositoryConfig.getRepoName(), repositoryConfig);
    }
    return BaseResponse.success(repositoryConfigMap);
  }

  @PostMapping("configPluginRepo")
  @ApiOperation("配置仓库参数")
  public BaseResponse<Boolean> configPluginRepo(@RequestBody PluginRepositoryProxy.RepositoryConfig repositoryConfig) throws Exception {
    return BaseResponse.success(pluginReporsitory.configPluginRepo(repositoryConfig));
  }

  @GetMapping("downloadToLocal")
  @ApiOperation("远程仓库插件下载到本地")
  public BaseResponse<Boolean> downloadToLocal(@RequestParam("pluginId") String pluginId, @RequestParam("version") String version, @RequestParam("repoType") String repoType) {
    pluginReporsitory.downloadPluginToLocal(pluginId, version, repoType);
    return BaseResponse.success(true);
  }
}