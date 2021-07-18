package mydataharbor.rpc.client;


import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import mydataharbor.rpc.codec.Beat;
import mydataharbor.rpc.codec.RpcRequest;
import mydataharbor.rpc.codec.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;

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
