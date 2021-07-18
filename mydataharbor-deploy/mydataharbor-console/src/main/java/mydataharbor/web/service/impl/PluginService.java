package mydataharbor.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import mydataharbor.web.dao.IPluginDao;
import mydataharbor.web.entity.PluginEntity;
import mydataharbor.web.mapper.IPluginMapper;
import mydataharbor.web.service.IPluginService;
import org.apache.commons.lang3.StringUtils;
import org.pf4j.PluginDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @auth xulang
 * @Date 2021/6/28
 **/
@Component
public class PluginService implements IPluginService {

  @Autowired
  private IPluginDao pluginDao;

  @Autowired
  private IPluginMapper pluginMapper;

  @Override
  public PluginEntity uploadPlugin(String fileName, String filePath, PluginDescriptor pluginDescriptor) {
    PluginEntity pluginEntity = new PluginEntity();
    pluginEntity.setPluginId(pluginDescriptor.getPluginId());
    pluginEntity.setVersion(pluginDescriptor.getVersion());
    pluginEntity.setPluginDescription(pluginDescriptor.getPluginDescription());
    pluginEntity.setPluginStorePath(filePath);
    pluginEntity.setFileName(fileName);
    pluginDao.save(pluginEntity);
    return pluginEntity;
  }


  @Override
  public PluginEntity query(String pluginId, String version) {
    LambdaQueryWrapper<PluginEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.eq(PluginEntity::getPluginId, pluginId).eq(PluginEntity::getVersion, version);
    PluginEntity pluginEntity = pluginMapper.selectOne(lambdaQueryWrapper);
    return pluginEntity;
  }

  @Override
  public IPage<PluginEntity> pageQuery(IPage<PluginEntity> page, String pluginId, String version, String des) {
    LambdaQueryWrapper<PluginEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
    lambdaQueryWrapper.like(StringUtils.isNotBlank(pluginId), PluginEntity::getPluginId, pluginId);
    lambdaQueryWrapper.like(StringUtils.isNotBlank(version), PluginEntity::getVersion, version);
    lambdaQueryWrapper.like(StringUtils.isNotBlank(des), PluginEntity::getPluginDescription, des);
    return pluginMapper.selectPage(page, lambdaQueryWrapper);
  }

}
