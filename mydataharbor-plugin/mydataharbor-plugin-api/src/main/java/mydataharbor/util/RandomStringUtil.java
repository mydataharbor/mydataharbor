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


package mydataharbor.util;

import java.util.ArrayList;
import java.util.Random;

public class RandomStringUtil {

  public static ArrayList<String> strList = new ArrayList<String>();
  public static Random random = new Random();

  static {
    init();
  }

  public static String generate32RandomStr() {
    return generateRandomStr(32);
  }

  public static String generateRandomStr(int length) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; i++) {
      int size = strList.size();
      String randomStr = strList.get(random.nextInt(size));
      sb.append(randomStr);
    }
    return sb.toString();
  }

  private static void init() {
    int begin = 97;
    //生成小写字母,并加入集合
    for (int i = begin; i < begin + 26; i++) {
      strList.add((char) i + "");
    }
    //生成大写字母,并加入集合
    begin = 65;
    for (int i = begin; i < begin + 26; i++) {
      strList.add((char) i + "");
    }
    //将0-9的数字加入集合
    for (int i = 0; i < 10; i++) {
      strList.add(i + "");
    }
  }
}