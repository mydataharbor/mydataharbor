package mydataharbor.web.controller;

import mydataharbor.plugin.api.group.GroupInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.web.base.BaseResponse;
import mydataharbor.web.entity.PluginEntity;
import mydataharbor.web.service.INodeService;
import mydataharbor.web.service.IPluginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

@Api(tags = "node")
@RestController
@RequestMapping("/mydataharbor/node/")
@Slf4j
public class NodeController {

  @Autowired
  private INodeService nodeService;

  @Autowired
  private CompoundPluginDescriptorFinder compoundPluginDescriptorFinder;

  @Autowired
  private IPluginService pluginService;

  @ApiOperation("触发group节点变更通知")
  @GetMapping("groupTouch")
  public BaseResponse<Boolean> touchGroup(@RequestParam("groupName") String groupName) {
    nodeService.groupTouch(groupName, null);
    return BaseResponse.success(true);
  }

  /**
   * @return
   */
  @ApiOperation("查询集群内所有节点")
  @GetMapping("nodeList")
  public BaseResponse<Map<String, List<NodeInfo>>> listNodes() {
    return BaseResponse.success(nodeService.lisNode());
  }


  @ApiOperation("通过组名查询插件安装信息")
  @GetMapping("plugin")
  public BaseResponse<List<PluginInfo>> getPluginInfoByNodeName(@RequestParam("groupName") String groupName) {
    return BaseResponse.success(nodeService.getPluginInfoByGroupName(groupName));
  }

  @RequestMapping(value = "/installPlugin", method = RequestMethod.POST)
  @ResponseBody
  @ApiOperation("安装插件")
  public BaseResponse<PluginInfo> installPlugin(@RequestParam("groupName") String groupName, @RequestParam("pluginId") String pluginId, @RequestParam("version") String version, @RequestParam("sync") @ApiParam("是否同步上传") boolean sync) throws IOException {
    PluginEntity pluginEntity = pluginService.query(pluginId, version);
    if (pluginEntity == null) {
      throw new RuntimeException("请先上传插件！");
    }
    PluginDescriptor pluginDescriptor = compoundPluginDescriptorFinder.find(Paths.get(pluginEntity.getPluginStorePath()));
    byte[] bytes = FileUtils.readFileToByteArray(new File(pluginEntity.getPluginStorePath()));
    if (sync) {
      return BaseResponse.success(nodeService.installPluginByRpcUpload(pluginEntity.getFileName(), pluginDescriptor, bytes, groupName));
    } else {
      return BaseResponse.success(nodeService.installPluginByReporsitory(pluginId, version, pluginDescriptor, groupName));
    }
  }

  @PostMapping("uninstallPlugin")
  @ResponseBody
  @ApiOperation("卸载插件")
  public BaseResponse<Boolean> uninstallPlugin(@RequestParam("groupName") String groupName, @RequestParam("pluginId") String pluginId) throws RemoteException {
    return BaseResponse.success(nodeService.uninstallPlugin(pluginId, groupName));
  }

  @ApiOperation("查询集群内所有业务组")
  @GetMapping("groupList")
  public BaseResponse<Map<String, GroupInfo>> listGroupInfo() {
    return BaseResponse.success(nodeService.listGroupInfo());
  }


}