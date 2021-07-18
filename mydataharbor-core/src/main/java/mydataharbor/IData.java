package mydataharbor;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;

import java.util.List;

/**
 * 超级接口
 *
 * @auth xulang
 * @Date 2021/5/10
 **/
public interface IData {

  TypeResolver TYPE_RESOLVER = new TypeResolver();

  default Class getTypeByIndex(int index, String flag, Class clazz) {
    ResolvedType resolvedType = TYPE_RESOLVER.resolve(getClass());
    List<ResolvedType> resolvedTypes = resolvedType.typeParametersFor(clazz);
    return resolvedTypes.get(index).getErasedType();
    //   return ReflectUtil.getTypeByIndex(this.getClass(), index, flag, clazz);
  }


}
