package com.wangguo;


import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@Configuration // 表示该类是一个配置类，用于配置Bean和其他组件
@EnableSwagger2WebMvc // 这个注解启用Swagger2在Spring MVC应用程序中的功能，允许生成和展示API文档。或者使用专门为Knife4j提供的@EnableKnife4j注解。
public class Knife4jConfig {
    public Docket defultApi2() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        //.title("swagger-bootstrap-ui-demo RESTful APIs")
                        .description("idouyin短视频项目接口文档")
                        .termsOfServiceUrl("http://www.xx.com/")
                        .contact(new Contact("wangguo", "http://www.xxxx.com/", "1414516753@qq.com"))
                        .version("1.0")
                        .build())
                //分组名称K
                .groupName("2.X版本")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.wangguo.controller"))
                .paths(PathSelectors.any())
                .build();
        return docket;
    }
}
