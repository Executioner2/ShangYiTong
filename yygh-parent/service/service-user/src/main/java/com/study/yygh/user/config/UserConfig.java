package com.study.yygh.user.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 17:26
 * Versions:1.0.0
 * Description: mybatisPlus的配置类
 */
@Configuration
@MapperScan("com.study.yygh.user.mapper")
public class UserConfig {
}
