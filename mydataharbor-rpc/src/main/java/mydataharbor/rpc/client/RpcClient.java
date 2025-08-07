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


package mydataharbor.rpc.client;

import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClient {
  private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

  private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
    600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

  public static <T, P> T createService(Class<T> interfaceClass, String ip, String port) {
    return (T) Proxy.newProxyInstance(
      interfaceClass.getClassLoader(),
      new Class<?>[]{interfaceClass},
      new ObjectProxy<T, P>(interfaceClass, "1.0",  ip, port)
    );
  }



  public static void submit(Runnable task) {
    threadPoolExecutor.submit(task);
  }

}