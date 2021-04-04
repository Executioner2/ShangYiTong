package com.study.yygh.msm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 18:15
 * Versions:1.0.0
 * Description:
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class) // 不连接数据库，取消数据源自动配置，不取消会报错
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.study")
public class ServiceSmsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsApplication.class, args);
    }
}
