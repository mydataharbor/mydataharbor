package mydataharbor.web.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
@org.hibernate.annotations.Table(appliesTo = "mydataharbor_plugin", comment = "插件信息表")
@Table(name = "mydataharbor_plugin", indexes = {@Index(name = "plugin_unique", columnList = "plugin_id,version", unique = true)})
@Data
@TableName("mydataharbor_plugin")
public class PluginEntity {

  /**
   * 主键
   */
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /**
   * 插件id
   */
  @Column(columnDefinition = "varchar(255)", name = "plugin_id")
  private String pluginId;

  /**
   * 插件版本
   */
  @Column(columnDefinition = "varchar(255)", name = "version")
  private String version;

  /**
   * 文件名
   */
  @Column(columnDefinition = "varchar(255)", name = "file_name")
  private String fileName;

  /**
   * 插件描述
   */
  @Column(columnDefinition = "varchar(1255)", name = "plugin_description")
  private String pluginDescription;

  /**
   * 插件存储地址
   */
  @Column(columnDefinition = "varchar(1255)", name = "plugin_store_path")
  private String pluginStorePath;

  /**
   * 创建时间
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(updatable = false, name = "create_time")
  @CreationTimestamp
  private Date createTime;

  /**
   * 更新时间
   */
  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "update_time")
  private Date updateTime;


}