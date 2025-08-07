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


package mydataharbor.classutil.test;

import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;
import mydataharbor.classutil.classresolver.FieldTypeResolver;
import mydataharbor.classutil.classresolver.TypeInfo;
import org.junit.Test;

/**
 * @auth xulang
 * @Date 2021/6/22
 **/
public class FieldTypeResolverTest {

  @Test
  public void test() {
    TypeResolver typeResolver = new TypeResolver();
    ResolvedType resolvedType = typeResolver.resolve(MyClass.class);
    FieldTypeResolver fieldTypeResolver = new FieldTypeResolver(typeResolver);
    TypeInfo typeInfo = fieldTypeResolver.resolveClass(resolvedType);
    System.out.println(typeInfo);

    MemberResolver memberResolver = new MemberResolver(typeResolver);
    ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver.resolve(resolvedType, null, null);
    ResolvedConstructor[] constructors = resolvedTypeWithMembers.getConstructors();
    for (ResolvedConstructor constructor : constructors) {
      int argumentCount = constructor.getArgumentCount();

      System.out.println(constructor);
    }

  }

}