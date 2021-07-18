package mydataharbor.web.dao;

import mydataharbor.web.entity.PluginEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @auth xulang
 * @Date 2021/6/28
 **/
@Repository
public interface IPluginDao extends JpaRepository<PluginEntity, Long> {

}
