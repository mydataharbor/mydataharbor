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