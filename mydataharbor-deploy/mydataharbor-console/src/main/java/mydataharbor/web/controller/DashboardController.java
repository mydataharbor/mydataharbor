package mydataharbor.web.controller;

import com.google.common.collect.ImmutableMap;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.util.VersionUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 仪表盘信息输出
 * Created by xulang on 2021/9/1.
 */
@Api(tags = "node")
@RestController
@RequestMapping("/mydataharbor/dashboard/")
@Slf4j
public class DashboardController {

  @GetMapping("version")
  public Map<String, String> getVersion() {
    return ImmutableMap.of("console-version", VersionUtil.getVersion() == null ? "dev" : VersionUtil.getVersion());
  }
}
