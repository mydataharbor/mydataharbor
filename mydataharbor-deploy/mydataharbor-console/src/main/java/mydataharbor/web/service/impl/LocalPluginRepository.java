package mydataharbor.web.service.impl;

import com.github.zafarkhaja.semver.Version;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.constant.Constant;
import mydataharbor.web.entity.RepoPlugin;
import mydataharbor.web.entity.PluginGroup;
import mydataharbor.web.entity.PluginId;
import mydataharbor.web.entity.reporsitory.AuthResponse;
import mydataharbor.web.exception.NoAuthException;
import mydataharbor.web.pf4j.MyDataHarborPluginDescriptor;
import mydataharbor.web.service.IPluginRepository;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.VersionManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 本地存储库
 * Created by xulang on 2021/8/25.
 */
@Slf4j
@Repository(LocalPluginRepository.REPO_TYPE)
public class LocalPluginRepository extends AbstractPluginRepository implements InitializingBean {

  private Map<String, Path> fileMap = new HashMap<>();

  private Map<String, PluginGroup> pluginGroupMap = new HashMap<>();

  private Map<String, MyDataHarborPluginDescriptor> myDataHarborPluginDescriptorMap = new HashMap<>();

  public static final String REPO_TYPE = "LocalFileSystem-Plugin-Reporsitory";

  @Autowired
  private PluginDescriptorFinder pluginDescriptorFinder;

  public LocalPluginRepository() {
    super(null);
    this.reporsitoryPath = Constant.PLUGIN_PATH;
  }

  /**
   * 生成monitor,一intervalSeconds监听directory文件夹下面的以suffix结束，prefix开头的文件
   *
   * @param directory       监视的文件夹
   * @param intervalSeconds 轮训时间
   * @param suffix          监视文件的后缀
   * @param prefix          监视文件的前缀
   * @return 文件观察者
   */
  public FileAlterationMonitor getMonitor(File directory, Long intervalSeconds, String prefix, String suffix, FileAlterationListener fileAlterationListener) {
    long interval = TimeUnit.SECONDS.toMillis(intervalSeconds);
    IOFileFilter directories = FileFilterUtils.and(FileFilterUtils.directoryFileFilter());
    IOFileFilter suffixFilter = FileFilterUtils.suffixFileFilter(suffix);
    IOFileFilter prefixFilter = FileFilterUtils.prefixFileFilter(prefix);
    IOFileFilter files = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
      suffixFilter, prefixFilter);
    IOFileFilter filter = FileFilterUtils.or(directories, files);
    FileAlterationObserver observer = new FileAlterationObserver(directory, filter);
    observer.addListener(fileAlterationListener);
    FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
    return monitor;
  }

  /**
   * 插件库目录
   */
  private String reporsitoryPath;

  @Override
  public String name() {
    return "本地存储库";
  }

  @Override
  List<PluginGroup> doListPluginGroup() {
    return pluginGroupMap.values().stream().collect(Collectors.toList());
  }

  @Override
  public RepoPlugin doQuery(String pluginId, String pluginVersion) {
    Path path = fileMap.get(pluginId + pluginVersion);
    if (path == null) {
      return null;
    }
    MyDataHarborPluginDescriptor pluginDescriptor = myDataHarborPluginDescriptorMap.get(path.toFile().getPath());
    PluginGroup pluginGroup = pluginGroupMap.get(pluginDescriptor.getPluginGroup());
    if (pluginGroup != null) {
      for (PluginId plugin : pluginGroup.getPlugins()) {
        if (plugin.getPluginId().equals(pluginId)) {
          for (RepoPlugin pluginRepoPlugin : plugin.getRepoPlugins()) {
            if (pluginRepoPlugin.getVersion().equals(pluginVersion)) {
              return pluginRepoPlugin;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public boolean isAuth(String pluginId, String version) {
    return true;
  }

  @Override
  public AuthResponse auth(String pluginId, String version) {
    return new AuthResponse(true, "本地存储库自动授权");
  }

  public Path getPath(String pluginId, String version) {
    return fileMap.get(pluginId + version);
  }

  @Override
  public InputStream doFetchPlugin(String pluginId, String version) throws NoAuthException, IOException {
    Path path = fileMap.get(pluginId + version);
    if (path == null)
      throw new IOException("本地没有该插件！");
    return new FileInputStream(path.toFile());
  }

  @Override
  public void upload(String fileName, String pluginId, String version, InputStream inputStream) throws IOException {
    RepoPlugin repoPlugin = doQuery(pluginId, version);
    if (repoPlugin != null) {
      Version semverVersion = Version.valueOf(repoPlugin.getVersion());
      String preReleaseVersion = semverVersion.getPreReleaseVersion();
      if (StringUtils.isBlank(preReleaseVersion))
        throw new RuntimeException("该版本的插件已经存在！");
    }
    File reporsitoryPath = FileUtils.getFile(this.reporsitoryPath);
    if (!reporsitoryPath.exists()) {
      reporsitoryPath.mkdirs();
    }
    File pluginFile = new File(reporsitoryPath, fileName);
    if (pluginFile.exists()) {
      pluginFile.delete();
    }
    pluginFile.createNewFile();
    FileOutputStream fileOutputStream = new FileOutputStream(pluginFile);
    try {
      byte[] buffer = new byte[1024];
      int read = 0;
      while ((read = inputStream.read(buffer)) > 0) {
        fileOutputStream.write(buffer, 0, read);
      }
    } finally {
      fileOutputStream.close();
      inputStream.close();
    }
  }

  @Override
  public IPluginRepository next() {
    return null;
  }

  private void updatePluginCache(File file, MyDataHarborPluginDescriptor pluginDescriptor) {
    PluginGroup pluginGroup = pluginGroupMap.get(pluginDescriptor.getPluginGroup());
    if (pluginGroup == null) {
      pluginGroup = new PluginGroup();
      String pluginGroupName = pluginDescriptor.getPluginGroup();
      pluginGroupName = StringUtils.isBlank(pluginGroupName) ? "default" : pluginGroupName;
      pluginGroup.setGroupName(pluginGroupName);
      pluginGroup.setImageBase64(pluginDescriptor.getPluginGroupLogo());
      pluginGroup.setRepoName(name());
      pluginGroup.setRepoType(REPO_TYPE);
      pluginGroupMap.put(pluginDescriptor.getPluginGroup(), pluginGroup);
    }
    List<PluginId> plugins = pluginGroup.getPlugins();
    List<PluginId> pluginIdList = plugins.stream().filter(pluginId -> pluginId.getPluginId().equals(pluginDescriptor.getPluginId())).collect(Collectors.toList());
    PluginId pluginId;
    if (CollectionUtils.isEmpty(pluginIdList)) {
      pluginId = new PluginId();
      pluginId.setPluginId(pluginDescriptor.getPluginId());
      plugins.add(pluginId);
    } else {
      pluginId = pluginIdList.get(0);
    }
    RepoPlugin repoPlugin = new RepoPlugin();
    repoPlugin.setProvider(pluginDescriptor.getProvider());
    repoPlugin.setFileName(file.getName());
    repoPlugin.setMydataharborVersion(pluginDescriptor.getMydataharborVersion());
    repoPlugin.fillByPluginDescriptor(pluginDescriptor);
    List<RepoPlugin> existRepoPlugins = pluginId.getRepoPlugins().stream().filter(plugin1 -> plugin1.getVersion().equals(repoPlugin.getVersion())).collect(Collectors.toList());
    pluginId.getRepoPlugins().removeAll(existRepoPlugins);
    pluginId.getRepoPlugins().add(repoPlugin);
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    File file = FileUtils.getFile(reporsitoryPath);
    if (!file.exists()) {
      file.mkdirs();
    }
    SimpleFileAlterationListener simpleFileAlterationListener = new SimpleFileAlterationListener();
    FileAlterationMonitor monitor = getMonitor(file, 1L, "", ".jar", simpleFileAlterationListener);
    try {
      monitor.start();
      for (File listFile : file.listFiles()) {
        simpleFileAlterationListener.onFileCreate(listFile);
      }
    } catch (Exception e) {
      log.error("启动文件监听器失败", e);
    }
  }


  public class SimpleFileAlterationListener implements FileAlterationListener {

    @Override
    public void onStart(FileAlterationObserver observer) {

    }

    @Override
    public void onDirectoryCreate(File directory) {

    }

    @Override
    public void onDirectoryChange(File directory) {

    }

    @Override
    public void onDirectoryDelete(File directory) {

    }

    public void waitFileWriteComplete(File file) {
      long oldLen = 0;
      long newLen = 0;
      while (true) {
        newLen = file.length();
        if ((newLen - oldLen) > 0) {
          oldLen = newLen;
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            log.error("线程被打断", e);
          }
        } else {
          break;
        }
      }
    }

    @Override
    public void onFileCreate(File file) {
      log.info("plugin add : {}", file);
      waitFileWriteComplete(file);
      try {
        MyDataHarborPluginDescriptor pluginDescriptor = (MyDataHarborPluginDescriptor) pluginDescriptorFinder.find(file.toPath());
        fileMap.put(pluginDescriptor.getPluginId() + pluginDescriptor.getVersion(), file.toPath());
        updatePluginCache(file, pluginDescriptor);
        myDataHarborPluginDescriptorMap.put(file.getPath(), pluginDescriptor);
      } catch (Exception e) {
        log.error("处理文件变更异常", e);
      }
    }

    @Override
    public void onFileChange(File file) {
      log.info("plugin change : {}", file);
      try {
        MyDataHarborPluginDescriptor pluginDescriptor = (MyDataHarborPluginDescriptor) pluginDescriptorFinder.find(file.toPath());
        fileMap.put(pluginDescriptor.getPluginId() + pluginDescriptor.getVersion(), file.toPath());
        updatePluginCache(file, pluginDescriptor);
        myDataHarborPluginDescriptorMap.put(file.getPath(), pluginDescriptor);
      } catch (Exception e) {
        log.error("处理文件变更异常", e);
      }

    }

    @Override
    public void onFileDelete(File file) {
      log.info("plugin delete : {}", file);
      try {
        MyDataHarborPluginDescriptor pluginDescriptor = myDataHarborPluginDescriptorMap.get(file.getPath());
        fileMap.remove(pluginDescriptor.getPluginId() + pluginDescriptor.getVersion());
        PluginGroup pluginGroup = pluginGroupMap.get(pluginDescriptor.getPluginGroup());
        if (pluginGroup != null) {
          List<PluginId> existPluginIds = pluginGroup.getPlugins().stream().filter(pluginId -> pluginId.getPluginId().equals(pluginDescriptor.getPluginId())).collect(Collectors.toList());
          for (PluginId existPluginId : existPluginIds) {
            List<RepoPlugin> repoPlugins = existPluginId.getRepoPlugins();
            if (repoPlugins != null) {
              repoPlugins.removeIf(plugin -> plugin.getVersion().equals(pluginDescriptor.getVersion()));
            }
          }
        }
      } catch (Exception e) {
        log.error("处理文件变更异常", e);
      }

    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }
  }

}
