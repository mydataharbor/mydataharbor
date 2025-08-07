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