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
