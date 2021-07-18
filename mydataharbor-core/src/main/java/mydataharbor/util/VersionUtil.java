package mydataharbor.util;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @auth xulang
 * @Date 2021/7/12
 **/
@Slf4j
public class VersionUtil {
  public static String getVersion() {
    Package p = VersionUtil.class.getPackage();
    return p.getImplementationVersion();
  }
}
