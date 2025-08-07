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


package mydataharbor.web.configuration;

import io.swagger.annotations.ApiOperation;
import mydataharbor.web.pf4j.MyDataHarborManifestPluginDescriptorFinder;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import org.pf4j.CompoundPluginDescriptorFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

  @Bean
  public Docket createRestApi() {
    return new Docket(DocumentationType.OAS_30)
      .apiInfo(apiInfo())
      .select()
      .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
      .paths(PathSelectors.any())
      .build();
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder()
      .title("数据港口接口文档")
      .description("更多请咨询服务开发者")
      .contact(new Contact("mydataharbor", "https://www.mydataharbor.com", "1053618636@qq.com"))
      .version("1.0")
      .build();
  }

  @Bean
  public CompoundPluginDescriptorFinder compoundPluginDescriptorFinder() {
    CompoundPluginDescriptorFinder compoundPluginDescriptorFinder = new CompoundPluginDescriptorFinder()
      .add(new MyDataHarborManifestPluginDescriptorFinder());
    return compoundPluginDescriptorFinder;
  }

}