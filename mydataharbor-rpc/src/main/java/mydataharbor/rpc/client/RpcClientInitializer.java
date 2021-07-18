package mydataharbor.rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import mydataharbor.rpc.codec.*;
import mydataharbor.rpc.serializer.Serializer;
import mydataharbor.rpc.serializer.protostuff.ProtostuffSerializer;

import java.util.concurrent.TimeUnit;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
  @Override
  protected void initChannel(SocketChannel socketChannel) throws Exception {
    Serializer serializer = ProtostuffSerializer.class.newInstance();
    ChannelPipeline cp = socketChannel.pipeline();
    cp.addLast(new IdleStateHandler(0, 0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
    cp.addLast(new RpcEncoder(RpcRequest.class, serializer));
    cp.addLast(new LengthFieldBasedFrameDecoder(200 * 1024 * 1024, 0, 4, 0, 0));
    cp.addLast(new RpcDecoder(RpcResponse.class, serializer));
    cp.addLast(new RpcClientHandler());
  }
}
