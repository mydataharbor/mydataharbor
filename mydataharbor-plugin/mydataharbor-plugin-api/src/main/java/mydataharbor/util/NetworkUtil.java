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


package mydataharbor.util;

import lombok.extern.slf4j.Slf4j;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * 网络util
 *
 * @auth xulang
 * @Date 2021/6/17
 **/
@Slf4j
public class NetworkUtil {

  /**
   * 获取本机可用ip
   *
   * @return
   */
  public static String getIpAddress() {
    try {
      Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
      InetAddress ip = null;
      while (allNetInterfaces.hasMoreElements()) {
        NetworkInterface netInterface = allNetInterfaces.nextElement();
        if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
          continue;
        } else {
          Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
          while (addresses.hasMoreElements()) {
            ip = addresses.nextElement();
            if (ip != null && ip instanceof Inet4Address) {
              return ip.getHostAddress();
            }
          }
        }
      }
    } catch (Exception e) {
      log.error("ip地址获取失败", e);
      throw new RuntimeException("ip地址获取失败", e);
    }
    throw new RuntimeException("获取不到可用的ip");
  }

}