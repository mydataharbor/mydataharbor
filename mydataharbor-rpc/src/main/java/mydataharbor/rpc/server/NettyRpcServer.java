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


package mydataharbor.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import mydataharbor.rpc.util.ServiceUtil;
import mydataharbor.rpc.util.ThreadPoolUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyRpcServer extends IRpcServer {
  private static final Logger logger = LoggerFactory.getLogger(NettyRpcServer.class);

  private String serverAddress;
  private Map<String, Object> serviceMap = new HashMap<>();

  private Channel channel;

  private Thread thread;

  private volatile Boolean bindSuccess = null;

  public NettyRpcServer(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  @Override
  public void addService(String interfaceName, String version, Object serviceBean) {
    logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
    String serviceKey = ServiceUtil.makeServiceKey(interfaceName, version);
    serviceMap.put(serviceKey, serviceBean);
  }

  public void start() throws InterruptedException {

    thread = new Thread(new Runnable() {
      ThreadPoolExecutor threadPoolExecutor = ThreadPoolUtil.makeServerThreadPool(
        NettyRpcServer.class.getSimpleName(), 16, 32);

      @Override
      public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
          ServerBootstrap bootstrap = new ServerBootstrap();
          bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .childHandler(new RpcServerInitializer(serviceMap, threadPoolExecutor))
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

          String[] array = serverAddress.split(":");
          String host = array[0];
          int port = Integer.parseInt(array[1]);
          ChannelFuture future = bootstrap.bind(host, port).sync();
          channel = future.channel();
          logger.info("Server started on port {}", port);
          bindSuccess = true;
          future.channel().closeFuture().sync();
        } catch (Exception e) {
          bindSuccess = false;
          if (e instanceof InterruptedException) {
            logger.info("Rpc server remoting server stop");
          } else {
            logger.error("Rpc server remoting server error", e);
            throw new RuntimeException("Rpc server remoting server error", e);
          }
        } finally {
          try {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
          } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
          }
        }
      }
    });
    thread.start();
    while (bindSuccess == null) {
      Thread.sleep(100);
    }
    if (!bindSuccess) {
      throw new RuntimeException("端口绑定失败！");
    }
  }

  public void stop() {
    // destroy server thread
    if (thread != null && thread.isAlive()) {
      thread.interrupt();
    }
  }
}