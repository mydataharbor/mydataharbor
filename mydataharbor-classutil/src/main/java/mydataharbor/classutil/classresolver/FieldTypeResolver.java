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


package mydataharbor.classutil.classresolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.Annotations;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedField;

/**
 * 类型解析
 *
 * @auth xulang
 * @Date 2021/6/21
 **/
public class FieldTypeResolver {

  private TypeResolver typeResolver;

  public FieldTypeResolver(TypeResolver typeResolver) {
    this.typeResolver = typeResolver;
  }

  public TypeInfo resolveClass(ResolvedType resolvedType) {
    Map<String, TypeInfo> visist = new HashMap<>();
    TypeInfo typeInfo = new TypeInfo();
    doResolveClass(visist, resolvedType, typeInfo);
    if (!typeInfo.isBaseType()) {
      //如果不是基础类型，统一走属性解析
      resolveField(visist, typeInfo);
    }
    return typeInfo;
  }

  private boolean isBaseType(ResolvedType resolvedType) {
    return resolvedType.isPrimitive()
      || resolvedType.isInstanceOf(Enum.class)
      || resolvedType.isInstanceOf(String.class)
      || resolvedType.isInstanceOf(Boolean.class)
      || resolvedType.isInstanceOf(Character.class)
      || resolvedType.isInstanceOf(Number.class);
  }

  private void doResolveClass(Map<String, TypeInfo> visist, ResolvedType resolvedType, TypeInfo typeInfo) {
    String fieldName = "";
    if (typeInfo instanceof FieldInfo) {
      fieldName = ((FieldInfo) typeInfo).getFieldName();
    }
    TypeInfo cacheTypeInfo = visist.get(resolvedType + fieldName);
    if (cacheTypeInfo != null) {
      typeInfo.setArray(resolvedType.isInstanceOf(Collection.class) || resolvedType.isArray());
      typeInfo.setBaseType(true);
      typeInfo.setClazzStr(cacheTypeInfo.getClazzStr());
      typeInfo.setType(cacheTypeInfo.getType());
      typeInfo.setJavaBase(isBaseType(resolvedType));
      typeInfo.setMap(resolvedType.isInstanceOf(Map.class));
      typeInfo.setFieldInfos(cacheTypeInfo.getFieldInfos());
      typeInfo.setEnumeration(resolvedType.isInstanceOf(Enum.class));
    }else {
      visist.put(resolvedType + fieldName, typeInfo);
      if (resolvedType.isInstanceOf(Collection.class)) {
        //集合处理
        typeInfo.setArray(true);
        List<ResolvedType> resolvedTypes = resolvedType.typeParametersFor(Collection.class);
        ResolvedType parameterType = resolvedTypes.get(0);
        typeInfo.setClazzStr(parameterType.getTypeName());
        typeInfo.setType(parameterType);
        typeInfo.setJavaBase(isBaseType(parameterType));
      } else if (resolvedType.isArray()) {
        //数组处理
        typeInfo.setArray(true);
        ResolvedType arrayElementType = resolvedType.getArrayElementType();
        typeInfo.setClazzStr(arrayElementType.getTypeName());
        typeInfo.setType(arrayElementType);
        typeInfo.setJavaBase(isBaseType(arrayElementType));
      } else if (resolvedType.isInstanceOf(Map.class)) {
        //map处理
        List<ResolvedType> resolvedTypes = resolvedType.typeParametersFor(Map.class);
        ResolvedType valueType = resolvedTypes.get(1);
        typeInfo.setMap(true);
        typeInfo.setType(valueType);
        typeInfo.setJavaBase(isBaseType(valueType));
        typeInfo.setClazzStr(valueType.getTypeName());
      } else if (resolvedType.isInstanceOf(Enum.class)) {
        //处理枚举
        typeInfo.setClazzStr(resolvedType.getTypeName());
        typeInfo.setEnumeration(true);
        typeInfo.setJavaBase(isBaseType(resolvedType));
        typeInfo.setType(resolvedType);
      } else {
        typeInfo.setClazzStr(resolvedType.getTypeName());
        typeInfo.setType(resolvedType);
        typeInfo.setJavaBase(isBaseType(resolvedType));
      }
    }
  }

  /**
   * 递归解析
   *
   * @param visist
   * @param typeInfo
   */
  private void resolveField(Map<String, TypeInfo> visist, TypeInfo typeInfo) {
    ResolvedType resolvedType = null;
    if (typeInfo.getType() instanceof ResolvedType) {
      resolvedType = (ResolvedType) typeInfo.getType();
    }

    if (typeInfo.isArray() || typeInfo.isMap()) {
      //数组，默认生成一个arry的字段
      // resolvedType.get
      List<FieldInfo> fieldInfos = new ArrayList<>();
      FieldInfo fieldInfo = new FieldInfo("0", "元素1");
      if (typeInfo.getType() instanceof ResolvedType) {
        doResolveClass(visist, (ResolvedType) typeInfo.getType(), fieldInfo);
      }
      if (!fieldInfo.isBaseType()) {
        resolveField(visist, fieldInfo);
      }
      fieldInfos.add(fieldInfo);
      typeInfo.setFieldInfos(fieldInfos);
    } else {
      //普通java包装类处理
      if (resolvedType != null) {
        MemberResolver memberResolver = new MemberResolver(typeResolver);
        AnnotationConfiguration annConfig = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_BUT_DONT_INHERIT);
        ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver.resolve(resolvedType, annConfig, null);
        ResolvedField[] memberFields = resolvedTypeWithMembers.getMemberFields();
        List<FieldInfo> fieldInfos = new ArrayList<>();
        for (ResolvedField memberField : memberFields) {
          if (memberField.isTransient()) {
            continue;
          }
          Annotations annotations = memberField.getAnnotations();
          MyDataHarborMarker myDataHarborMarker = annotations.get(MyDataHarborMarker.class);
          FieldInfo fieldInfo = new FieldInfo(memberField.getName());
          if (myDataHarborMarker != null) {
            fieldInfo.setTitle(myDataHarborMarker.title());
            fieldInfo.setDes(myDataHarborMarker.des());
            fieldInfo.setRequire(myDataHarborMarker.require());
            fieldInfo.setDefaultValue(myDataHarborMarker.defaultValue());
          }
          doResolveClass(visist, memberField.getType(), fieldInfo);
          if(fieldInfo.isEnumeration()){
            Object[] enumConstants = memberField.getType().getErasedType().getEnumConstants();
            List<String> candidateValue = Arrays.stream(enumConstants).map(Object::toString).collect(Collectors.toList());
            fieldInfo.setCandidateValue(candidateValue);
          }
          if (!fieldInfo.isBaseType()) {
            resolveField(visist, fieldInfo);
          }
          fieldInfos.add(fieldInfo);
        }
        typeInfo.setFieldInfos(fieldInfos);
      }
    }
    visist.remove(resolvedType);
  }
}