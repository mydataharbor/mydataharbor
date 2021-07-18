package mydataharbor.rpc.server;

public abstract class IRpcServer {
  /**
   * start server
   *
   * @param
   * @throws Exception
   */
  public abstract void start() throws InterruptedException;

  /**
   * @param interfaceName
   * @param version
   * @param serviceBean
   */
  public abstract void addService(String interfaceName, String version, Object serviceBean);

  /**
   * 停止
   */
  public abstract void stop();

}
