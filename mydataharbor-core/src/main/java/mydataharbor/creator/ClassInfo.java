package mydataharbor.creator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ClassInfo implements Serializable {

  private String clazz;

  private String title;

  private List<ConstructorAndArgsConfig> constructorAndArgsConfigs;
}