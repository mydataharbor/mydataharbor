package mydataharbor.rpc.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import mydataharbor.rpc.codec.*;
import mydataharbor.rpc.serializer.protostuff.ProtostuffSerializer;
import mydataharbor.rpc.serializer.Serializer;

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
