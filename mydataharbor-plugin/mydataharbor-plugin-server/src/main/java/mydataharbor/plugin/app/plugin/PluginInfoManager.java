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


package mydataharbor.plugin.app.plugin;

import lombok.extern.slf4j.Slf4j;
import mydataharbor.IData;
import mydataharbor.IDataConverter;
import mydataharbor.IDataPipelineCreator;
import mydataharbor.IDataSink;
import mydataharbor.IDataSource;
import mydataharbor.IProtocolDataChecker;
import mydataharbor.IProtocolDataConverter;
import mydataharbor.classutil.classresolver.FieldTypeResolver;
import mydataharbor.classutil.classresolver.MyDataHarborMarker;
import mydataharbor.classutil.classresolver.TypeInfo;
import mydataharbor.pipeline.creator.ClassInfo;
import mydataharbor.pipeline.creator.ConstructorAndArgsConfig;
import mydataharbor.plugin.api.IPluginInfoManager;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.api.plugin.DataPipelineCreatorInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.pf4j.PluginWrapper;

import com.fasterxml.classmate.AnnotationConfiguration;
import com.fasterxml.classmate.AnnotationInclusion;
import com.fasterxml.classmate.MemberResolver;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.ResolvedTypeWithMembers;
import com.fasterxml.classmate.TypeResolver;
import com.fasterxml.classmate.members.ResolvedConstructor;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
@Slf4j
public class PluginInfoManager implements IPluginInfoManager {


  private IPluginServer pluginServer;

  private TypeResolver typeResolver = new TypeResolver();

  private FieldTypeResolver fieldTypeResolver = new FieldTypeResolver(typeResolver);

  /**
   * 第一层是 plugin，第二层是 class
   */
  private Map<String, Map<String, IDataPipelineCreator>> dataPipelineCreatorMap = new ConcurrentHashMap<>();

  private volatile List<PluginInfo> pluginInfos = new ArrayList<>();

  public PluginInfoManager(IPluginServer pluginServer) {
    this.pluginServer = pluginServer;
  }

  @Override
  public List<PluginInfo> getAllPluginInfos() {
    return pluginInfos;
  }


  @Override
  public void refresh() throws InstantiationException, IllegalAccessException {
    dataPipelineCreatorMap.clear();
    this.pluginInfos = scanAllPluginInfo();
  }

  @Override
  public Map<String, IDataPipelineCreator> scanDataPipelineCreatorByPlugin(String pluginId) {
    return dataPipelineCreatorMap.get(pluginId);
  }

  @Override
  public List<PluginInfo> scanAllPluginInfo() throws IllegalAccessException, InstantiationException {
    List<PluginWrapper> plugins = pluginServer.getPluginManager().getPlugins();
    List<PluginInfo> pluginInfos = Collections.synchronizedList(new ArrayList<>());
    for (PluginWrapper plugin : plugins) {
      PluginInfo pluginInfo = new PluginInfo();
      pluginInfo.fillByPluginDescriptor(plugin.getDescriptor());
      List<DataPipelineCreatorInfo> dataPipelineCreatorInfos = new ArrayList<>();
      List<Class<? extends IDataPipelineCreator>> dataSinkCreatorAllClazz = plugin.getPluginManager().getExtensionClasses(IDataPipelineCreator.class, plugin.getPluginId());
      Set<Class<? extends IDataPipelineCreator>> canCreator = dataSinkCreatorAllClazz.stream()
        .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
        .collect(Collectors.toSet());
        for (Class<? extends IDataPipelineCreator> aClass : canCreator) {
            try {
                DataPipelineCreatorInfo dataPipelineCreatorInfo = creatorProcess(aClass, pluginInfo);
                dataPipelineCreatorInfos.add(dataPipelineCreatorInfo);
            } catch (Throwable e){
                log.error("扫描creator下可用资源异常！", e);
            }
        }
      pluginInfo.setDataPipelineCreatorInfos(dataPipelineCreatorInfos);
      pluginInfos.add(pluginInfo);
    }
    return pluginInfos;
  }

  public DataPipelineCreatorInfo creatorProcess(Class<? extends IDataPipelineCreator> aClass, PluginInfo pluginInfo) throws InstantiationException, IllegalAccessException {
    Map<String, IDataPipelineCreator> dataPipelineCreatorMap = this.dataPipelineCreatorMap.get(pluginInfo.getPluginId());
    if (dataPipelineCreatorMap == null) {
      dataPipelineCreatorMap = new ConcurrentHashMap<>();
      this.dataPipelineCreatorMap.put(pluginInfo.getPluginId(), dataPipelineCreatorMap);
    }
    IDataPipelineCreator dataPipelineCreator = dataPipelineCreatorMap.get(generateClazzInfo(aClass, pluginInfo));
    if (dataPipelineCreator == null) {
      dataPipelineCreator = aClass.newInstance();
      dataPipelineCreatorMap.put(generateClazzInfo(aClass, pluginInfo), dataPipelineCreator);
    }
    DataPipelineCreatorInfo dataPipelineCreatorInfo = new DataPipelineCreatorInfo();
    dataPipelineCreatorInfo.setClazz(generateClazzInfo(aClass, pluginInfo));
    dataPipelineCreatorInfo.setType(dataPipelineCreator.type());
    dataPipelineCreatorInfo.setCanCreatePipeline(dataPipelineCreator.canCreatePipeline());
    ResolvedType resolvedType = typeResolver.resolve(dataPipelineCreator.getClass());
    List<ResolvedType> resolvedTypes = resolvedType.typeParametersFor(IDataPipelineCreator.class);
    ResolvedType configResolveType = resolvedTypes.get(0);
    ResolvedType settingResolveType = resolvedTypes.get(1);
    TypeInfo configClassInfo = fieldTypeResolver.resolveClass(configResolveType);
    TypeInfo settingClassInfo = fieldTypeResolver.resolveClass(settingResolveType);
    dataPipelineCreatorInfo.setConfigClassInfo(configClassInfo);
    dataPipelineCreatorInfo.setSettingClassInfo(settingClassInfo);

    Set<Class> availableDataSource = dataPipelineCreator.availableDataSource();
    List<ClassInfo> dataSourceClassInfo = classProcess(availableDataSource);
    dataPipelineCreatorInfo.setDataSourceClassInfo(dataSourceClassInfo);

    Set<Class> availableProtocolConverter = dataPipelineCreator.availableProtocolDataConverter();
    List<ClassInfo> protocolConverterClassInfo = classProcess(availableProtocolConverter);
    dataPipelineCreatorInfo.setProtocolConverterClassInfo(protocolConverterClassInfo);

    Set<Class> availableDataConverter = dataPipelineCreator.availableDataConverter();
    List<ClassInfo> dataConverterClassInfo = classProcess(availableDataConverter);
    dataPipelineCreatorInfo.setDataConverterClassInfo(dataConverterClassInfo);

    Set<Class> availableDataChecker = dataPipelineCreator.availableDataChecker();
    List<ClassInfo> dataCheckerClassInfo = classProcess(availableDataChecker);
    dataPipelineCreatorInfo.setCheckerClassInfo(dataCheckerClassInfo);

    Set<Class> availableDataSink = dataPipelineCreator.availableDataSink();
    List<ClassInfo> dataSinkClassInfo = classProcess(availableDataSink);
    dataPipelineCreatorInfo.setDataSinkClassInfo(dataSinkClassInfo);

    return dataPipelineCreatorInfo;
  }

  private List<ClassInfo> classProcess(Set<Class> clazzes) {
    List<ClassInfo> classInfos = new ArrayList<>();
    for (Class clazz : clazzes) {
      ClassInfo classInfo = getClassInfo(clazz);
      classInfos.add(classInfo);
    }
    return classInfos;
  }

  @NotNull
  private ClassInfo getClassInfo(Class clazz) {
    ResolvedType resolvedType = typeResolver.resolve(clazz);
    ClassInfo classInfo = new ClassInfo();
    classInfo.setClazz(clazz.getTypeName());
    MemberResolver memberResolver = new MemberResolver(typeResolver);
    AnnotationConfiguration annConfig = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_BUT_DONT_INHERIT);
    ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver.resolve(resolvedType, annConfig, null);
    MyDataHarborMarker myDataHarborMarker = (MyDataHarborMarker) clazz.getAnnotation(MyDataHarborMarker.class);
    if (myDataHarborMarker != null) {
      classInfo.setTitle(myDataHarborMarker.title());
    }
    ResolvedConstructor[] constructors = resolvedTypeWithMembers.getConstructors();
    List<ConstructorAndArgsConfig> constructorAndArgsConfigs = new ArrayList<>();
    classInfo.setConstructorAndArgsConfigs(constructorAndArgsConfigs);
    for (ResolvedConstructor constructor : constructors) {
      ConstructorAndArgsConfig constructorAndArgsConfig = new ConstructorAndArgsConfig();
      constructorAndArgsConfig.setConstructorName(constructor.getName());
      constructorAndArgsConfigs.add(constructorAndArgsConfig);
      constructorAndArgsConfig.setArgsType(new ArrayList<>());
      constructorAndArgsConfig.setArgsTypeInfo(new ArrayList<>());
      int argumentCount = constructor.getArgumentCount();
      constructorAndArgsConfig.setArgsCount(argumentCount);
      for (int i = 0; i < argumentCount; i++) {
        ResolvedType argumentType = constructor.getArgumentType(i);
        TypeInfo typeInfo = fieldTypeResolver.resolveClass(argumentType);
        constructorAndArgsConfig.getArgsType().add(argumentType.getTypeName());
        constructorAndArgsConfig.getArgsTypeInfo().add(typeInfo);
      }
    }

    //再解析
    if(IDataSource.class.isAssignableFrom(clazz)){
      Class tClass = IData.getTypeByClass(0, clazz, IDataSource.class);
      Class sClass = IData.getTypeByClass(1, clazz, IDataSource.class);
      classInfo.setTClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(tClass)));
      classInfo.setSClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(sClass)));
    }

    if(IProtocolDataConverter.class.isAssignableFrom(clazz)){
      Class tClass = IData.getTypeByClass(0, clazz, IProtocolDataConverter.class);
      Class pClass = IData.getTypeByClass(1, clazz, IProtocolDataConverter.class);
      Class sClass = IData.getTypeByClass(2, clazz, IProtocolDataConverter.class);
      classInfo.setTClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(tClass)));
      classInfo.setPClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(pClass)));
      classInfo.setSClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(sClass)));
    }

    if(IProtocolDataChecker.class.isAssignableFrom(clazz)){
      Class pClass = IData.getTypeByClass(0, clazz, IProtocolDataChecker.class);
      Class sClass = IData.getTypeByClass(1, clazz, IProtocolDataChecker.class);
      classInfo.setPClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(pClass)));
      classInfo.setSClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(sClass)));
    }

    if(IDataConverter.class.isAssignableFrom(clazz)){
      Class pClass = IData.getTypeByClass(0, clazz, IDataConverter.class);
      Class rClass = IData.getTypeByClass(1, clazz, IDataConverter.class);
      Class sClass = IData.getTypeByClass(2, clazz, IDataConverter.class);
      classInfo.setPClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(pClass)));
      classInfo.setRClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(rClass)));
      classInfo.setSClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(sClass)));
    }

    if(IDataSink.class.isAssignableFrom(clazz)){
      Class rClass = IData.getTypeByClass(0, clazz, IDataSink.class);
      Class sClass = IData.getTypeByClass(1, clazz, IDataSink.class);
      classInfo.setRClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(rClass)));
      classInfo.setSClassInfo(fieldTypeResolver.resolveClass(typeResolver.resolve(sClass)));
    }

    return classInfo;
  }


  public String generateClazzInfo(Class clazz, PluginInfo pluginInfo) {
    ResolvedType resolvedType = typeResolver.resolve(clazz);
    return resolvedType.getTypeName();
  }

}