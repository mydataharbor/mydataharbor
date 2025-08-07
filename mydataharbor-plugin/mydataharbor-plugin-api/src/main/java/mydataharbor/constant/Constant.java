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


package mydataharbor.constant;

/**
 * @auth xulang
 * @Date 2021/6/11
 **/
public class Constant {

  public static final String NODE_PREFIX = "/mydataharbor";

    public static final String NODE_NAME = "node";

    public static final String LEADER = "leader";

    public static final String CONFIG_FILE_PATH = "config";

    public static final String CONFIG_FILE_NAME = "system.yml";

    public static final String PLUGIN_PATH = "mydataharbor-console-plugins/";
  
    public static final String PLUGIN_PATH_WORKER = "mydataharbor-server-plugins/";

    public static final String PLUGIN_DOWNLOAD_PATH = "/mydataharbor/plugin/downloadPlugin";

    public static final String TASK_PATH = "task";

    public static final String TASK_PATH_PARENT = Constant.NODE_PREFIX + "/" + Constant.TASK_PATH + "/";

    public static final String TASK_DATA_STORAGE_PATH = Constant.NODE_PREFIX + "/task-storage" + "/";

    public static final String TASK_DATA_STORAGE_LOCK_PATH = Constant.NODE_PREFIX + "/task-storage" + "/lock/";

  public static final String NODE_GROUP_PATH = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/";

  public static final String PLUGIN_REPOSITORY_CONFIG_FILE_NAME = "repo.json";

  public static final String LOCK_PATH = Constant.NODE_PREFIX + "/" + "lock" + "/";
}
