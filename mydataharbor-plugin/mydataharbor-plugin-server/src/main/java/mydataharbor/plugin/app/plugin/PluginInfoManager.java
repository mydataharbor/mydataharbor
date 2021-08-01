package mydataharbor.plugin.app.plugin;

import com.fasterxml.classmate.*;
import com.fasterxml.classmate.members.ResolvedConstructor;
import mydataharbor.IDataSinkCreator;
import mydataharbor.creator.ClassInfo;
import mydataharbor.creator.ConstructorAndArgsConfig;
import mydataharbor.plugin.api.IPluginInfoManager;
import mydataharbor.plugin.api.IPluginServer;
import mydataharbor.plugin.api.plugin.DataSinkCreatorInfo;
import mydataharbor.plugin.api.plugin.PluginInfo;
import mydataharbor.classutil.classresolver.FieldMarker;
import mydataharbor.classutil.classresolver.FieldTypeResolver;
import mydataharbor.classutil.classresolver.TypeInfo;
import org.pf4j.PluginWrapper;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @auth xulang
 * @Date 2021/6/30
 **/
public class PluginInfoManager implements IPluginInfoManager {


  private IPluginServer pluginServer;

  private TypeResolver typeResolver = new TypeResolver();

  private FieldTypeResolver fieldTypeResolver = new FieldTypeResolver(typeResolver);

  /**
   * 第一层是 plugin，第二层是 class
   */
  private Map<String, Map<String, IDataSinkCreator>> dataSinkCreatorMap = new ConcurrentHashMap<>();

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
    this.pluginInfos = scanAllPluginInfo();
  }

  @Override
  public Map<String, IDataSinkCreator> getDataSinkCreatorMapByPlugin(String pluginId) {
    return dataSinkCreatorMap.get(pluginId);
  }

  @Override
  public List<PluginInfo> scanAllPluginInfo() throws IllegalAccessException, InstantiationException {
    List<PluginWrapper> plugins = pluginServer.getPluginManager().getPlugins();
    List<PluginInfo> pluginInfos = Collections.synchronizedList(new ArrayList<>());
    for (PluginWrapper plugin : plugins) {
      PluginInfo pluginInfo = new PluginInfo();
      pluginInfo.fillByPluginDescriptor(plugin.getDescriptor());
      List<DataSinkCreatorInfo> dataSinkCreatorInfos = new ArrayList<>();
      List<Class<? extends IDataSinkCreator>> dataSinkCreatorAllClazz = plugin.getPluginManager().getExtensionClasses(IDataSinkCreator.class, plugin.getPluginId());
      Set<Class<? extends IDataSinkCreator>> canCreator = dataSinkCreatorAllClazz.stream()
        .filter(clazz -> !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers()))
        .collect(Collectors.toSet());
      for (Class<? extends IDataSinkCreator> aClass : canCreator) {
        DataSinkCreatorInfo dataSinkCreatorInfo = creatorProcess(aClass, pluginInfo);
        dataSinkCreatorInfos.add(dataSinkCreatorInfo);
      }
      pluginInfo.setDataSinkCreatorInfos(dataSinkCreatorInfos);
      pluginInfos.add(pluginInfo);
    }
    return pluginInfos;
  }

  public DataSinkCreatorInfo creatorProcess(Class<? extends IDataSinkCreator> aClass, PluginInfo pluginInfo) throws InstantiationException, IllegalAccessException {
    Map<String, IDataSinkCreator> stringIDataSinkCreatorMap = dataSinkCreatorMap.get(pluginInfo.getPluginId());
    if (stringIDataSinkCreatorMap == null) {
      stringIDataSinkCreatorMap = new ConcurrentHashMap<>();
      dataSinkCreatorMap.put(pluginInfo.getPluginId(), stringIDataSinkCreatorMap);
    }
    IDataSinkCreator dataSinkCreator = stringIDataSinkCreatorMap.get(generateClazzInfo(aClass, pluginInfo));
    if (dataSinkCreator == null) {
      dataSinkCreator = aClass.newInstance();
      stringIDataSinkCreatorMap.put(generateClazzInfo(aClass, pluginInfo), dataSinkCreator);
    }
    DataSinkCreatorInfo dataSinkCreatorInfo = new DataSinkCreatorInfo();
    dataSinkCreatorInfo.setClazz(generateClazzInfo(aClass, pluginInfo));
    dataSinkCreatorInfo.setType(dataSinkCreator.type());
    dataSinkCreatorInfo.setCanCreatePipline(dataSinkCreator.canCreatePipline());
    ResolvedType resolvedType = typeResolver.resolve(dataSinkCreator.getClass());
    List<ResolvedType> resolvedTypes = resolvedType.typeParametersFor(IDataSinkCreator.class);
    ResolvedType configResolveType = resolvedTypes.get(0);
    TypeInfo configClassInfo = fieldTypeResolver.resolveClass(configResolveType);
    dataSinkCreatorInfo.setConfigClassInfo(configClassInfo);
    Set<Class> availableDataSource = dataSinkCreator.availableDataSource();
    List<ClassInfo> dataSourceClassInfo = classProcess(availableDataSource);
    dataSinkCreatorInfo.setDataSourceClassInfo(dataSourceClassInfo);

    Set<Class> availableProtocalConventor = dataSinkCreator.availableDataProtocalConventor();
    List<ClassInfo> protocalConventorClassInfo = classProcess(availableProtocalConventor);
    dataSinkCreatorInfo.setProtocalConvertorClassInfo(protocalConventorClassInfo);

    Set<Class> availableDataConventor = dataSinkCreator.avaliableDataConventor();
    List<ClassInfo> dataConventorClassInfo = classProcess(availableDataConventor);
    dataSinkCreatorInfo.setDataConvertorClassInfo(dataConventorClassInfo);

    Set<Class> availableDataChecker = dataSinkCreator.avaliableDataChecker();
    List<ClassInfo> dataCheckerClassInfo = classProcess(availableDataChecker);
    dataSinkCreatorInfo.setCheckerClassInfo(dataCheckerClassInfo);

    Set<Class> availableDataSink = dataSinkCreator.avaliableDataSink();
    List<ClassInfo> dataSinkClassInfo = classProcess(availableDataSink);
    dataSinkCreatorInfo.setDataSinkClassInfo(dataSinkClassInfo);

    Set<Class> avaliableSettingContext = dataSinkCreator.avaliableSettingContext();
    List<ClassInfo> settingContextClassInfo = classProcess(avaliableSettingContext);
    dataSinkCreatorInfo.setSettingClassInfo(settingContextClassInfo);
    return dataSinkCreatorInfo;
  }

  private List<ClassInfo> classProcess(Set<Class> clazzes) {
    List<ClassInfo> classInfos = new ArrayList<>();
    for (Class clazz : clazzes) {
      ResolvedType resolvedType = typeResolver.resolve(clazz);
      ClassInfo classInfo = new ClassInfo();
      classInfo.setClazz(clazz.getTypeName());
      MemberResolver memberResolver = new MemberResolver(typeResolver);
      AnnotationConfiguration annConfig = new AnnotationConfiguration.StdConfiguration(AnnotationInclusion.INCLUDE_BUT_DONT_INHERIT);
      ResolvedTypeWithMembers resolvedTypeWithMembers = memberResolver.resolve(resolvedType, annConfig, null);
      FieldMarker fieldMarker = (FieldMarker) clazz.getAnnotation(FieldMarker.class);
      if (fieldMarker != null) {
        classInfo.setTitle(fieldMarker.value());
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
      classInfos.add(classInfo);
    }
    return classInfos;
  }


  public String generateClazzInfo(Class clazz, PluginInfo pluginInfo) {
    ResolvedType resolvedType = typeResolver.resolve(clazz);
    return resolvedType.getTypeName();
  }

}
