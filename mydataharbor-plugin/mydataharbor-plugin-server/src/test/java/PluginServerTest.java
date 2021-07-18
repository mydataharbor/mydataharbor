import lombok.extern.slf4j.Slf4j;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.app.pluginserver.PluginServerImpl;
import mydataharbor.util.VersionUtil;
import org.junit.Test;

/**
 * @auth xulang
 * @Date 2021/7/18
 **/
@Slf4j
public class PluginServerTest {

  @Test
  public void test() {
    IPluginServer pluginServer = new PluginServerImpl(PluginServerTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    try {
      pluginServer.start();
    } catch (Throwable throwable) {
      log.error("启动时发生错误！", throwable);
      System.exit(-1);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> pluginServer.stop()));
    pluginServer.startDaemonAwaitThread();
    log.info("系统启动成功！");
  }
}
