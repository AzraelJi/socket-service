package com.yang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@Configuration
public class SwaggerConfig {

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("websocket").description("描述：socket api").version("版本：1.0.0")
                .contact(new Contact("yang", "", "sun.ji@zenlayer.com")).build();
    }

    @Bean
    public Docket myServiceApi() {
        return new Docket(DocumentationType.SWAGGER_2).groupName("rest API接口文档").apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.yang.controller")).build();

    }
}
