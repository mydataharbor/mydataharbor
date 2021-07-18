/*
 * Copyright (C) 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package mydataharbor.server;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import lombok.extern.slf4j.Slf4j;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.app.pluginserver.PluginServerImpl;
import mydataharbor.util.VersionUtil;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.rmi.RemoteException;

/**
 * 数据港口基座服务
 *
 * @author xulang
 */
@Slf4j
public class MyDataHarborServerBoot {
  public static void main(String[] args) throws RemoteException {
    System.out.println("\n" +
      "--      __  ___             ____            __             __  __                    __                 \n" +
      "--     /  |/  /   __  __   / __ \\  ____ _  / /_  ____ _   / / / /  ____ _   _____   / /_   ____    _____\n" +
      "--    / /|_/ /   / / / /  / / / / / __ `/ / __/ / __ `/  / /_/ /  / __ `/  / ___/  / __ \\ / __ \\  / ___/\n" +
      "--   / /  / /   / /_/ /  / /_/ / / /_/ / / /_  / /_/ /  / __  /  / /_/ /  / /     / /_/ // /_/ / / /    \n" +
      "--  /_/  /_/    \\__, /  /_____/  \\__,_/  \\__/  \\__,_/  /_/ /_/   \\__,_/  /_/     /_.___/ \\____/ /_/     \n" +
      "--             /____/                                                                                   \n");
    System.out.println(" :: MyDataHarbor ::        (" + VersionUtil.getVersion() + ")" + "\n");
    initLog();
    IPluginServer pluginServer = new PluginServerImpl(MyDataHarborServerBoot.class.getProtectionDomain().getCodeSource().getLocation().getPath());
    try {
      pluginServer.start();
    } catch (Throwable throwable) {
      log.error("启动时发生错误！", throwable);
      System.exit(-1);
    }
    Runtime.getRuntime().addShutdownHook(new Thread(() -> pluginServer.stop()));
    pluginServer.startDaemonAwaitThread();
    log.info("系统启动成功！");
  }

  private static void initLog() {
    File logbackFile = new File("./config/logback.xml");
    if (logbackFile.exists()) {
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      try {
        configurator.doConfigure(logbackFile);
      } catch (JoranException e) {
        e.printStackTrace(System.err);
        System.exit(-1);
      }
    }
  }
}
