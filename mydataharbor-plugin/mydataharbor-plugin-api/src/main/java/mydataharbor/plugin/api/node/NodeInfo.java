package mydataharbor.plugin.api.node;

import mydataharbor.plugin.api.plugin.PluginServerConfig;
import mydataharbor.plugin.api.exception.RenameException;
import mydataharbor.util.NetworkUtil;
import mydataharbor.util.RandomStringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 机器节点信息
 *
 * @auth xulang
 * @Date 2021/6/11
 **/
@Data
@Slf4j
public class NodeInfo extends JvmSystemInfo {

  /**
   * 节点名
   */
  private String nodeName;

  /**
   * ip
   */
  private String ip;

  /**
   * 端口
   */
  private Integer port;

  /**
   * 主机名
   */
  private String hostName;

  /**
   * 启动时间
   */
  private long startTime = System.currentTimeMillis();

  /**
   * 启动jar路径
   */
  private String runJarPath;

  /**
   * 是否静态节点名
   */
  private boolean staticNodeName = false;

  /**
   * 分组
   */
  private String group;

  /**
   * 运行任务数
   */
  private AtomicLong taskNum = new AtomicLong();

  /**
   * 该节点是否为leader
   */
  private boolean leader = false;

  /**
   * 版本
   */
  private String version;

  public NodeInfo() {

  }

  public NodeInfo(PluginServerConfig pluginServerConfig) {
    if (StringUtils.isBlank(pluginServerConfig.getNodeName())) {
      this.nodeName = RandomStringUtil.generateRandomStr(10);
    } else {
      this.staticNodeName = true;
      this.nodeName = pluginServerConfig.getNodeName();
    }
    this.group = pluginServerConfig.getGroup();
    this.port = pluginServerConfig.getPort();
    if (StringUtils.isNotBlank(pluginServerConfig.getIp())) {
      this.ip = pluginServerConfig.getIp();
    } else {
      //自动获取本机ip
      this.ip = NetworkUtil.getIpAddress();
    }
    try {
      InetAddress ia = InetAddress.getLocalHost();
      this.hostName = ia.getHostName();
    } catch (UnknownHostException e) {
      log.error("获取主机名失败", e);
    }
  }

  /**
   * 重新命名
   */
  public void rename() throws RenameException {
    if (staticNodeName) {
      throw new RenameException("用户采用静态节点名配置，无法重新命名");
    }
    this.nodeName = RandomStringUtil.generateRandomStr(10);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    NodeInfo nodeInfo = (NodeInfo) o;
    return Objects.equals(nodeName, nodeInfo.nodeName) &&
      Objects.equals(ip, nodeInfo.ip) &&
      Objects.equals(port, nodeInfo.port) &&
      Objects.equals(hostName, nodeInfo.hostName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), nodeName, ip, port, hostName);
  }
}
