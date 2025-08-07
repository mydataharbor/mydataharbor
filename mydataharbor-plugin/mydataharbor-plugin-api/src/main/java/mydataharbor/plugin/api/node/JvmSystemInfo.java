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


package mydataharbor.plugin.api.node;

import lombok.Data;

import java.util.Properties;

/**
 * @auth xulang
 * @Date 2021/6/17
 **/
@Data
public class JvmSystemInfo {

  /**
   * Java的运行环境版本：
   */
  private String javaVersion;
  /**
   * Java的运行环境供应商：
   */
  private String javaVendor;
  /**
   * Java供应商的URL：
   */
  private String javaVendorUrl;
  /**
   * Java的安装路径：
   */
  private String javaHome;


  /**
   * Java的虚拟机规范版本
   */
  private String javaVmSpecificationVersion;

  /**
   * Java的虚拟机规范供应商：
   **/
  private String javaVmSpecificationVendor;

  /**
   * Java的虚拟机规范名称：
   **/

  private String javaVmSpecificationName;
  /**
   * Java的虚拟机实现版本：
   **/
  private String javaVmVersion;

  /**
   * Java的虚拟机实现供应商：
   **/
  private String javaVmVendor;


  /**
   * Java的虚拟机实现名称：
   **/
  private String javaVmName;

  /**
   * Java运行时环境规范版本：
   **/
  private String javaSpecificationVersion;

  /**
   * Java运行时环境规范供应商：
   **/
  private String javaSpecificationVender;

  /**
   * Java运行时环境规范名称：
   **/
  private String javaSpecificationName;

  /**
   * Java的类格式版本号：
   **/
  private String javaClassVersion;
  /**
   * Java的类路径：
   **/
  //private String javaClassPath;
  /**
   * 加载库时搜索的路径列表：
   **/
//  private String javaLibraryPath;

  /**
   * 默认的临时文件路径：
   **/
  private String javaIoTmpdir;

  /**
   * 一个或多个扩展目录的路径：
   **/
  private String javaExtDirs;

  /**
   * 操作系统的名称：
   **/
  private String osName;

  /**
   * 操作系统的构架：
   **/
  private String osArch;
  /**
   * 操作系统的版本：
   **/
  private String osVersion;

  /**
   * 文件分隔符：
   **/
  private String fileSeparator;

  /**
   * 行分隔符：
   **/
  private String lineSeparator;

  /**
   * 路径分隔符：
   **/
  private String pathSeparator;
  /**
   * 用户的账户名称：
   **/
  private String userName;
  /**
   * 用户的主目录：
   **/
  private String userHome;

  /**
   * 用户的当前工作目录：
   **/
  private String userDir;

  public JvmSystemInfo() {
    Properties props = System.getProperties();
    this.javaVersion = props.getProperty("java.version");
    this.javaVendor = props.getProperty("java.vendor");
    this.javaVendorUrl = props.getProperty("java.vendor.url");
    this.javaHome = props.getProperty("java.home");
    this.javaVmSpecificationVersion = props.getProperty("java.vm.specification.version");
    this.javaVmSpecificationVendor = props.getProperty("java.vm.specification.vendor");
    this.javaVmSpecificationName = props.getProperty("java.vm.specification.name");
    this.javaVmVersion = props.getProperty("java.vm.version");
    this.javaVmVendor = props.getProperty("java.vm.vendor");
    this.javaVmName = props.getProperty("java.vm.name");
    this.javaSpecificationVersion = props.getProperty("java.specification.version");
    this.javaSpecificationVender = props.getProperty("java.specification.vender");
    this.javaSpecificationName = props.getProperty("java.specification.name");
    this.javaClassVersion = props.getProperty("java.class.version");
   // this.javaClassPath = props.getProperty("java.class.path");
  //  this.javaLibraryPath = props.getProperty("java.library.path");
    this.javaIoTmpdir = props.getProperty("java.io.tmpdir");
    this.javaExtDirs = props.getProperty("java.ext.dirs");
    this.osName = props.getProperty("os.name");
    this.osArch = props.getProperty("os.arch");
    this.osVersion = props.getProperty("os.version");
    this.fileSeparator = props.getProperty("file.separator");
    this.pathSeparator = props.getProperty("path.separator");
    this.lineSeparator = props.getProperty("line.separator");
    this.userName = props.getProperty("user.name");
    this.userHome = props.getProperty("user.home");
    this.userDir = props.getProperty("user.dir");
  }
}