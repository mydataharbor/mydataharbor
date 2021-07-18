package mydataharbor.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.oas.annotations.EnableOpenApi;

@EnableOpenApi
@SpringBootApplication
@MapperScan("mydataharbor.web.mapper")
public class MyDataHarborConsole {

  public static void main(String[] args) {
    SpringApplication.run(MyDataHarborConsole.class, args);
  }

}
