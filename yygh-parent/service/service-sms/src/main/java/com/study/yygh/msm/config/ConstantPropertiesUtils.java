package com.study.yygh.msm.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 18:23
 * Versions:1.0.0
 * Description:
 */
@Component
public class ConstantPropertiesUtils implements InitializingBean {
    @Value("${aliyun.sms.regionId}")
    private String regionId;

    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.sms.secret}")
    private String secret;

    public static String REGION_Id;
    public static String ACCESS_KEY_ID;
    public static String SECRECT;

    /**
     * 实现 InitializingBean 接口，
     * 在初始化bean时（ConstantPropertiesUtils 被创建时）执行
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        REGION_Id=regionId;
        ACCESS_KEY_ID=accessKeyId;
        SECRECT=secret;
    }

}
