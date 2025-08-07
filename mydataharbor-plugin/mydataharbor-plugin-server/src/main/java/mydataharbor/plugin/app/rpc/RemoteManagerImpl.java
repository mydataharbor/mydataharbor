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


package mydataharbor.plugin.app.rpc;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.plugin.api.IPluginInfoManager;
import mydataharbor.plugin.api.IPluginRemoteManager;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.api.ITaskManager;
import mydataharbor.plugin.api.exception.PluginLoadException;
import mydataharbor.plugin.api.node.NodeInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.plugin.api.task.SingleTask;
import mydataharbor.plugin.api.task.TaskState;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;

/**
 * @auth xulang
 * @Date 2021/6/17
 **/
@Slf4j
public class RemoteManagerImpl implements IPluginRemoteManager {

  private IPluginServer pluginServer;

  private IPluginInfoManager pluginInfoManager;

  private ITaskManager taskManager;

  private OkHttpClient httpClient = new OkHttpClient().newBuilder()
    .connectTimeout(1, TimeUnit.SECONDS)
    .readTimeout(120, TimeUnit.SECONDS)
    .build();


  public RemoteManagerImpl(IPluginServer pluginServer, IPluginInfoManager pluginInfoManager, ITaskManager taskManager) {
    this.pluginServer = pluginServer;
    this.pluginInfoManager = pluginInfoManager;
    this.taskManager = taskManager;
    try {
      pluginInfoManager.refresh();
    } catch (Throwable e) {
      throw new PluginLoadException("扫描creator接口相关信息失败！！", e);
    }
  }

  @Override
  public void stop() {
    pluginServer.stop();
  }

  @Override
  public String loadPluginByRpc(String pluginFileName, byte[] body) {
      //保存插件文件
      NodeInfo nodeInfo = pluginServer.getNodeInfo();
      String path =  nodeInfo.getRunJarPath() + Constant.PLUGIN_PATH_WORKER;
      File pluginFile = new File(path + "/" + pluginFileName);
      if (pluginFile.exists()) {
          log.error(pluginFile.getPath() + "该文件已经存在！");
          throw new PluginLoadException(pluginFile.getPath() + "该文件已经存在！");
      }
      else {
          new File(path).mkdirs();
          try {
              pluginFile.createNewFile();
              FileOutputStream fileOutputStream = new FileOutputStream(pluginFile);
              fileOutputStream.write(body);
              fileOutputStream.close();
      } catch (IOException e) {
        log.error("插件文件数据写入失败！", e);
        throw new PluginLoadException("插件文件数据写入失败！", e);
      }
    }
    String plugin = pluginServer.getPluginManager().loadPlugin(Paths.get(pluginFile.toURI()));
    return plugin;
  }

  @Override
  public String loadPluginByRepository(String pluginId, String version) {
    log.info("安装插件:{}:{}", pluginId, version);
    if (StringUtils.isBlank(pluginServer.getPluginServerConfig().getPluginRepository())) {
      log.warn("没有配置repository地址，无法异步下载插件进行安装！");
      return null;
    }
    PluginWrapper plugin = pluginServer.getPluginManager().getPlugin(pluginId);
    if (plugin != null) {
      log.info("该插件已经安装！");
    } else {
      //文件下载
      log.info("开始下载插件{}，版本:{}...", pluginId, version);
      HttpUrl.Builder httpBuilder = HttpUrl.parse(pluginServer.getPluginServerConfig().getPluginRepository() + Constant.PLUGIN_DOWNLOAD_PATH)
        .newBuilder().addQueryParameter("pluginId", pluginId).addQueryParameter("version", version);
      Request request = new Request.Builder()
        .url(httpBuilder.build())
        .get()
        .build();
      try {
        Response response = httpClient.newCall(request).execute();
        if (response.isSuccessful()) {
          log.info("插件{}:{}文件下载成功，准备安装...", pluginId, version);
          byte[] bytes = response.body().bytes();
          String loadedPluginId = loadPluginByRpc(pluginId + "-" + version + "-plugin.jar", bytes);
          log.info("插件{},版本:{}，安装成功！", pluginId, version);
          return loadedPluginId;
        } else {
          log.error("文件下载失败！{}:{},{}", pluginId, version, response);
        }
      } catch (IOException e) {
        log.error("文件下载失败！pluginID：{}，version：{}", pluginId, version, e);
      }
    }
    return null;
  }

  @Override
  public boolean uninstallPlugin(String pluginId) {
    log.info("卸载插件:{}", pluginId);
    boolean deletePlugin = pluginServer.getPluginManager().deletePlugin(pluginId);
    try {
      pluginInfoManager.refresh();
    } catch (Throwable e) {
      throw new PluginLoadException("扫描creator接口相关信息失败！！", e);
    }
    return deletePlugin;
  }

  @Override
  public PluginState startPlugin(String pluginId) {
    PluginState pluginState = pluginServer.getPluginManager().startPlugin(pluginId);
    try {
      pluginInfoManager.refresh();
    } catch (Throwable e) {
      throw new PluginLoadException("扫描creator接口相关信息失败！！", e);
    }
    return pluginState;
  }


  @Override
  public List<PluginInfo> getAllPluginInfo() {
    return pluginInfoManager.getAllPluginInfos();
  }

  @Override
  public PluginInfo getPluginInfoByPluginId(String pluginId) {
    for (PluginInfo pluginInfo : getAllPluginInfo()) {
      if (pluginInfo.getPluginId().equals(pluginId)) {
        return pluginInfo;
      }
    }
    return null;
  }


  @Override
  public String createPipeline(SingleTask singleTask) {
    return taskManager.submitTask(singleTask);
  }

  @Override
  public TaskState queryTaskState(String taskId) {
    return taskManager.queryTaskState(taskId);
  }

  @Override
  public List<SingleTask> lisTask() {
    return taskManager.lisTask();
  }

  @Override
  public void manageTask(String taskId, TaskState taskState) {
    taskManager.manageTask(taskId, taskState);
  }


}
