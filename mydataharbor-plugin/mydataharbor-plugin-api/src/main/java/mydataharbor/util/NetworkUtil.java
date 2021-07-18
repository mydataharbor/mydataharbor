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
        NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
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
