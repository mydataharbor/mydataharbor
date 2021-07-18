package mydataharbor.rpc.client;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class RpcClient {
  private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

  private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
    600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

  public static <T, P> T createService(Class<T> interfaceClass, String ip, String port) {
    return (T) Proxy.newProxyInstance(
      interfaceClass.getClassLoader(),
      new Class<?>[]{interfaceClass},
      new ObjectProxy<T, P>(interfaceClass, "1.0",  ip, port)
    );
  }



  public static void submit(Runnable task) {
    threadPoolExecutor.submit(task);
  }

}

