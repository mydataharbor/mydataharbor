package mydataharbor.creator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class ConstructorAndArgs {
  private String pluginId;
  private String clazz;
  private List<String> argsType;
  private List<String> argsJsonValue;
}