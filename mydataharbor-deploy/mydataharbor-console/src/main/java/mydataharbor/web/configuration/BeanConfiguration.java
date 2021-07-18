package mydataharbor.web.configuration;

import io.swagger.annotations.ApiOperation;
import org.pf4j.CompoundPluginDescriptorFinder;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.PropertiesPluginDescriptorFinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

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
      .contact(new Contact("徐浪", "www.baidu.com", "1053618636@qq.com"))
      .version("1.0")
      .build();
  }

  @Bean
  public CompoundPluginDescriptorFinder compoundPluginDescriptorFinder() {
    CompoundPluginDescriptorFinder compoundPluginDescriptorFinder = new CompoundPluginDescriptorFinder()
      .add(new PropertiesPluginDescriptorFinder())
      .add(new ManifestPluginDescriptorFinder());
    return compoundPluginDescriptorFinder;
  }
}