package mydataharbor.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import mydataharbor.constant.Constant;
import mydataharbor.web.base.BaseResponse;
import mydataharbor.web.entity.PluginEntity;
import mydataharbor.web.service.IPluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import mydataharbor.rpc.util.JsonUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
  private IPluginService pluginService;

  @RequestMapping(value = "/uploadPlugin", method = {RequestMethod.POST}, headers = "content-type=multipart/form-data")
  @ResponseBody
  @ApiOperation("上传插件")
  public BaseResponse<PluginEntity> uploadPlugin(@ApiParam(value = "文件", required = true) @RequestPart("file") MultipartFile file) {
    if (file.isEmpty()) {
      throw new RuntimeException("上传失败，请选择文件");
    }
    try {
      String fileName = file.getOriginalFilename();
      new File(Constant.PLUGIN_PATH).mkdirs();
      Path path = Paths.get(Constant.PLUGIN_PATH, fileName);
      file.transferTo(Paths.get(Constant.PLUGIN_PATH, fileName));
      PluginDescriptor pluginDescriptor = compoundPluginDescriptorFinder.find(path);
      if (StringUtils.isBlank(pluginDescriptor.getPluginId()) || StringUtils.isBlank(pluginDescriptor.getVersion())) {
        throw new RuntimeException("插件不合法！");
      }
      PluginEntity pluginEntity = pluginService.uploadPlugin(fileName, path.toFile().getPath(), pluginDescriptor);
      return BaseResponse.success(pluginEntity);
    } catch (Exception e) {
      throw new RuntimeException("上传插件异常！:" + e.getMessage(), e);
    }

  }


  @GetMapping("/pageQuery")
  @ApiOperation("分页查询插件信息")
  public IPage<PluginEntity> pageQuery(Integer pageNo, Integer pageSize, String pluginId, String version, String des) {
    Page<PluginEntity> page = new Page<>(pageNo, pageSize);
    return pluginService.pageQuery(page, pluginId, version, des);
  }

  @GetMapping("/pageQueryForVue")
  @ApiOperation("分页查询插件信息,折叠")
  public IPage<PluginEntityExt> pageQueryForVue(Integer pageNo, Integer pageSize, String pluginId, String version, String des) {
    Page<PluginEntity> page = new Page<>(pageNo, pageSize);
    pluginService.pageQuery(page, pluginId, version, des);
    List<PluginEntity> records = page.getRecords();
    Map<String, PluginEntityExt> pluginEntityExtMap = new HashMap<>();
    for (PluginEntity record : records) {
      PluginEntityExt pluginEntityExt = pluginEntityExtMap.get(record.getPluginId());
      if (pluginEntityExt == null) {
        pluginEntityExt = JsonUtil.deserialize(JsonUtil.serialize(record), PluginEntityExt.class);
        pluginEntityExtMap.put(record.getPluginId(), pluginEntityExt);
      } else {
        pluginEntityExt.getChildren().add(record);
      }
    }
    page.setRecords(null);
    Page pageNew = JsonUtil.deserialize(JsonUtil.serialize(page), Page.class);
    pageNew.setRecords(Arrays.asList(pluginEntityExtMap.values().toArray()));
    return pageNew;
  }

  @Data
  public static class PluginEntityExt extends PluginEntity {
    private List<PluginEntity> children = new ArrayList<>();
  }


  @GetMapping("downloadPlugin")
  @ApiOperation("下载插件")
  public ResponseEntity<InputStreamResource> downloadPlugin(@RequestParam("pluginId") String pluginId, @RequestParam("version") String version)
    throws IOException {
    PluginEntity pluginEntity = pluginService.query(pluginId, version);
    if (pluginEntity == null) {
      return ResponseEntity.notFound().build();
    }
    FileSystemResource file = new FileSystemResource(pluginEntity.getPluginStorePath());
    if (!file.exists()) {
      return ResponseEntity.notFound().build();
    }
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", pluginEntity.getFileName()));
    headers.add("Pragma", "no-cache");
    headers.add("Expires", "0");
    return ResponseEntity
      .ok()
      .headers(headers)
      .contentLength(file.contentLength())
      .contentType(MediaType.parseMediaType("application/octet-stream"))
      .body(new InputStreamResource(file.getInputStream()));
  }
}
