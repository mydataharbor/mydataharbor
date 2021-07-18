package mydataharbor.rpc.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import mydataharbor.rpc.codec.RpcRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * @param <T>
 * @param <P>
 */
public class ObjectProxy<T, P> implements InvocationHandler {
  private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
  private Class<T> clazz;

  private String version = "1.0";

  private volatile RpcClientHandler handler;

  private static final EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);

  private String ip;

  private String port;

  public ObjectProxy(Class<T> clazz, String version, String ip, String port) {
    this.clazz = clazz;
    this.version = version;
    this.ip = ip;
    this.port = port;
    this.handler = createHandler(ip, port);
  }

  private RpcClientHandler createHandler(String ip, String port) {
    final InetSocketAddress remotePeer = new InetSocketAddress(ip, Integer.parseInt(port));
    Bootstrap b = new Bootstrap();
    b.group(eventLoopGroup)
      .channel(NioSocketChannel.class)
      .handler(new RpcClientInitializer());
    try {
      ChannelFuture channelFuture = b.connect(remotePeer);
      channelFuture.get();
      RpcClientHandler handler = channelFuture.channel().pipeline().get(RpcClientHandler.class);
      handler.setObjectProxy(this);
      return handler;
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("创建连接失败", e);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (Object.class == method.getDeclaringClass()) {
      String name = method.getName();
      if ("equals".equals(name)) {
        return proxy == args[0];
      } else if ("hashCode".equals(name)) {
        return System.identityHashCode(proxy);
      } else if ("toString".equals(name)) {
        return proxy.getClass().getName() + "@" +
          Integer.toHexString(System.identityHashCode(proxy)) +
          ", with InvocationHandler " + this;
      } else {
        throw new IllegalStateException(String.valueOf(method));
      }
    }

    RpcRequest request = new RpcRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setClassName(method.getDeclaringClass().getName());
    request.setMethodName(method.getName());
    request.setParameterTypes(method.getParameterTypes());
    request.setParameters(args);
    request.setVersion(version);
    // Debug
    if (logger.isDebugEnabled()) {
      logger.debug(method.getDeclaringClass().getName());
      logger.debug(method.getName());
      for (int i = 0; i < method.getParameterTypes().length; ++i) {
        logger.debug(method.getParameterTypes()[i].getName());
      }
      if (args != null)
        for (int i = 0; i < args.length; ++i) {
          logger.debug(args[i].toString());
        }
    }
    if (!handler.getChannel().isActive()) {
      reConnect();
    }
    RpcFuture rpcFuture = handler.sendRequest(request);
    return rpcFuture.get();
  }

  public void reConnect() {
    logger.info(ip + ":" + port + "重连...");
    try {
      this.handler = createHandler(ip, port);
      logger.info("重连成功！");
      return;
    } catch (Exception e) {
      logger.error("重连失败", e);
    }
  }


  private RpcRequest createRequest(String className, String methodName, Object[] args) {
    RpcRequest request = new RpcRequest();
    request.setRequestId(UUID.randomUUID().toString());
    request.setClassName(className);
    request.setMethodName(methodName);
    request.setParameters(args);
    request.setVersion(version);
    Class[] parameterTypes = new Class[args.length];
    // Get the right class type
    for (int i = 0; i < args.length; i++) {
      parameterTypes[i] = getClassType(args[i]);
    }
    request.setParameterTypes(parameterTypes);

    // Debug
    if (logger.isDebugEnabled()) {
      logger.debug(className);
      logger.debug(methodName);
      for (int i = 0; i < parameterTypes.length; ++i) {
        logger.debug(parameterTypes[i].getName());
      }
      for (int i = 0; i < args.length; ++i) {
        logger.debug(args[i].toString());
      }
    }

    return request;
  }

  private Class<?> getClassType(Object obj) {
    Class<?> classType = obj.getClass();
    return classType;
  }

}
