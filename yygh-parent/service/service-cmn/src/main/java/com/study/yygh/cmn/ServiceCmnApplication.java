package com.study.yygh.cmn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-19 19:15
 * Versions:1.0.0
 * Description:
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = "com.study")
public class ServiceCmnApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCmnApplication.class, args);
    }
}
