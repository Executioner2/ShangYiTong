package com.study.yygh.msm.service;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 18:26
 * Versions:1.0.0
 * Description:
 */
public interface SmsService {
    // 使用msm服务发送短信到用户手机
    boolean send(String phone, String code);
}
