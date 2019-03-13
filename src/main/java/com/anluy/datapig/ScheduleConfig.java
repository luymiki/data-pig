//package com.anluy.datapig;
//
//import org.apache.commons.lang3.concurrent.BasicThreadFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.env.Environment;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.SchedulingConfigurer;
//import org.springframework.scheduling.config.ScheduledTaskRegistrar;
//
//import java.util.concurrent.*;
//
///**
// * 定时器配置类，
// *
// * @author hc.zeng
// * @create 2017-05-31 17:19
// */
//@Configuration
//@EnableScheduling
//public class ScheduleConfig implements SchedulingConfigurer {
//    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleConfig.class);
//
//    @Autowired
//    Environment env;
//
//    @Override
//    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
//        //注册的需要ScheduledExecutorService类型
//        scheduledTaskRegistrar.setScheduler(taskExecutor());
//    }
//
//    /**
//     * 设置线程池参数
//     *
//     * @return
//     */
//    @Bean(destroyMethod = "shutdown")
//    public ScheduledThreadPoolExecutor taskExecutor() {
//        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(8,new BasicThreadFactory.Builder().namingPattern("data-pig-scheduled-%d").daemon(true).build());
////        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2,20,0L, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<>(128),new BasicThreadFactory.Builder().namingPattern("data-pig-scheduled-%d").daemon(true).build());
//        return scheduledThreadPoolExecutor;
//    }
//
//}
