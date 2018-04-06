package com.runcoding;

import com.google.common.base.Predicate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2 {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.runcoding.controller"))
                .paths(doFilteringRules())
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("Running Ghost中使用Swagger2构建RESTful APIs")
                .description("更多技术文章请关注：https://runcoding.github.io/")
                .termsOfServiceUrl("https://runcoding.github.io/")
                .contact(new Contact("Running Ghost","https://runcoding.github.io/","runcoding@163.com"))
                .version("1.0")
                .build();
    }

    /**
     * 设置过滤规则
     * 这里的过滤规则支持正则匹配
     * @return
     */
    private Predicate<String> doFilteringRules() {
        return  PathSelectors.any();
        /*
        指定目录显示接口
        return or(
                regex("/account.*")
        );*/
    }
}