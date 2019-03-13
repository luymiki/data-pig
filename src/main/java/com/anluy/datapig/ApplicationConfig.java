package com.anluy.datapig;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Created by hc.zeng 2017-2-20
 */
@Configuration
public class ApplicationConfig {

    @Autowired
    private Environment env;

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone(ZoneId.of("GMT+08")));
        MappingJackson2HttpMessageConverter bean = new MappingJackson2HttpMessageConverter(objectMapper);
        return bean;
    }

//    /**
//     * 初始化数据资源的配置文件
//     *
//     * @return
//     */
//    @Bean
//    public Object init() {
//        //初始化数据资源的配置文件
//        //鉴权信息配置
//        String url = env.getProperty("oauth.url");
//        String appPid = env.getProperty("oauth.appPid");
//        String appSecretKey = env.getProperty("oauth.appSecretKey");
//        String dataUrl = env.getProperty("ds.data.url");
//        if (StringUtils.isBlank(url)) {
//            throw new RuntimeException("ds oauth url is null");
//        }
//        if (StringUtils.isBlank(appPid)) {
//            throw new RuntimeException("ds oauth appPid is null");
//        }
//        if (StringUtils.isBlank(appSecretKey)) {
//            throw new RuntimeException("ds oauth appSecretKey is null");
//        }
//        if (StringUtils.isBlank(dataUrl)) {
//            throw new RuntimeException("ds oauth dataUrl is null");
//        }
//        return new Object();
//    }
}
