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

  public static final String PLUGIN_PATH = "plugins";

  public static final String PLUGIN_DOWNLOAD_PATH = "/mydataharbor/plugin/downloadPlugin";

  public static final String TASK_PATH = "task";

  public static final String TASK_PATH_PARENT = Constant.NODE_PREFIX + "/" + Constant.TASK_PATH + "/";

  public static final String NODE_GROUP_PATH = Constant.NODE_PREFIX + "/" + Constant.NODE_NAME + "/";
}
