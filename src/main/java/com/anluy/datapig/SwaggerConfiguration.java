/*
 * Copyright 2017 com.anluy
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.anluy.datapig;

import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * 功能说明：swagger 的配置信息
 * 访问路径：http://127.0.0.1:8080/swagger-ui.html
 * <p>
 * Created by hc.zeng on 2017/9/3.
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

    /**
     * api版本信息
     */
    public static final String VERSION = "1.0.0";

    /**
     * api的文本信息
     * @return
     */
    private static final ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("任务调度系统")
                .description("任务调度的API接口文档....")
                //.license("Apache 2.0")
                //.licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .termsOfServiceUrl("")
                .version(VERSION)
                //.contact(new Contact("","", "376160680@qq.com"))
                .build();
    }

    /**
     * api扫描配置信息，
     * @return
     */
    @Bean
    public Docket api() {
        //token共用参数
        List<Parameter> parameters = new ArrayList<>();
        ParameterBuilder authParameterBuilder = new ParameterBuilder();
        authParameterBuilder.name("authorization")
                .description("token")
                .modelRef(new ModelRef("String"))
                .parameterType("header")
                .defaultValue("Bearer")
                .required(false);
        parameters.add(authParameterBuilder.build());

        //返回错误码公共设置
        List<ResponseMessage> messages = new ArrayList<>();
        ResponseMessage message25001 = (new ResponseMessageBuilder()).message("参数校验异常").code(25001).build();
        ResponseMessage message29999 = (new ResponseMessageBuilder()).message("数据库异常").code(29999).build();
        ResponseMessage message10001 = (new ResponseMessageBuilder()).message("系统异常").code(10001).build();
        messages.add(message25001);
        messages.add(message29999);
        messages.add(message10001);

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class)) // 注解了 ApiOperation的方法纳入扫描范围
                .build()
                //.globalOperationParameters(parameters)
                .globalResponseMessage(RequestMethod.POST,messages)
                .globalResponseMessage(RequestMethod.GET,messages);
    }



}
