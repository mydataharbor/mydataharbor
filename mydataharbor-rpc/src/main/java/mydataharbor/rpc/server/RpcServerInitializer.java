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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import mydataharbor.rpc.codec.Beat;
import mydataharbor.rpc.codec.RpcDecoder;
import mydataharbor.rpc.codec.RpcEncoder;
import mydataharbor.rpc.codec.RpcRequest;
import mydataharbor.rpc.codec.RpcResponse;
import mydataharbor.rpc.serializer.Serializer;
import mydataharbor.rpc.serializer.protostuff.ProtostuffSerializer;

import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel> {
  private Map<String, Object> handlerMap;
  private ThreadPoolExecutor threadPoolExecutor;

  public RpcServerInitializer(Map<String, Object> handlerMap, ThreadPoolExecutor threadPoolExecutor) {
    this.handlerMap = handlerMap;
    this.threadPoolExecutor = threadPoolExecutor;
  }

  @Override
  public void initChannel(SocketChannel channel) throws Exception {
    Serializer serializer = ProtostuffSerializer.class.newInstance();
    ChannelPipeline cp = channel.pipeline();
    cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
    cp.addLast(new LengthFieldBasedFrameDecoder(200 * 1024 * 1024, 0, 4, 0, 0));
    cp.addLast(new RpcDecoder(RpcRequest.class, serializer));
    cp.addLast(new RpcEncoder(RpcResponse.class, serializer));
    cp.addLast(new RpcServerHandler(handlerMap, threadPoolExecutor));
  }
}