package com.study.yygh.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 22:16
 * Versions:1.0.0
 * Description:
 */
@SpringBootApplication
@ComponentScan(basePackages = {"com.study"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.study"})
public class ServiceOrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceOrderApplication.class, args);
    }
}
