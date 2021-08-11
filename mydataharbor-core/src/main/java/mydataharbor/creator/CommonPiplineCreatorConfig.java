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
public class CommonPiplineCreatorConfig {

  private ConstructorAndArgs dataSourceConstructorAndArgs;
  private ConstructorAndArgs protocalDataConvertorConstructorAndArgs;
  private List<ConstructorAndArgs> dataCheckerConstructorAndArgs;
  private ConstructorAndArgs dataConvertorConstructorAndArgs;
  private ConstructorAndArgs dataSinkConstructorAndArgs;
  private String settingContextClazz;
  private String settingContextJsonValue;

}