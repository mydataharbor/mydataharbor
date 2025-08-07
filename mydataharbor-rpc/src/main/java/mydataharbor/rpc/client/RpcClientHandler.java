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

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import mydataharbor.rpc.codec.Beat;
import mydataharbor.rpc.codec.RpcRequest;
import mydataharbor.rpc.codec.RpcResponse;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
  private static final Logger logger = LoggerFactory.getLogger(RpcClientHandler.class);

  private ConcurrentHashMap<String, RpcFuture> pendingRPC = new ConcurrentHashMap<>();
  private volatile Channel channel;
  private SocketAddress remotePeer;

  private ObjectProxy objectProxy;

  public void setObjectProxy(ObjectProxy objectProxy) {
    this.objectProxy = objectProxy;
  }

  public Channel getChannel() {
    return channel;
  }

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    this.remotePeer = this.channel.remoteAddress();
  }

  @Override
  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    super.channelRegistered(ctx);
    this.channel = ctx.channel();
  }

  @Override
  public void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
    String requestId = response.getRequestId();
    logger.debug("Receive response: " + requestId);
    RpcFuture rpcFuture = pendingRPC.get(requestId);
    if (rpcFuture != null) {
      pendingRPC.remove(requestId);
      rpcFuture.done(response);
    } else {
      logger.warn("Can not get pending response for request id: " + requestId);
    }
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    logger.error("Client caught exception: " + cause.getMessage());
    ctx.close();
  }

  public void close() {
    channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
  }

  public RpcFuture sendRequest(RpcRequest request) {
    RpcFuture rpcFuture = new RpcFuture(request);
    pendingRPC.put(request.getRequestId(), rpcFuture);
    try {
      ChannelFuture channelFuture = channel.writeAndFlush(request).sync();
      if (!channelFuture.isSuccess()) {
        logger.error("Send request {} error", request.getRequestId());
      }
    } catch (InterruptedException e) {
      logger.error("Send request exception: " + e.getMessage());
    }
    return rpcFuture;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    if (evt instanceof IdleStateEvent) {
      //Send ping
      sendRequest(Beat.BEAT_PING);
      logger.debug("Client send beat-ping to " + remotePeer);
    } else {
      super.userEventTriggered(ctx, evt);
    }
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    logger.warn("断线了！");
    objectProxy.reConnect();
  }
}