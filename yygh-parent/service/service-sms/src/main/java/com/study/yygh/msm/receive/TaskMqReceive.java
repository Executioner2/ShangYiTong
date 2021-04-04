package com.study.yygh.msm.receive;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-04 15:06
 * Versions:1.0.0
 * Description: 给用户发送定时消息
 */
@EnableBinding(Sink.class)
public class TaskMqReceive {

    /**
     * 定时任务执行方法
     */
    @StreamListener(Sink.INPUT)
    public void patientTips(Message<String> message){
        // TODO 后续改为给用户发送邮件通知
        System.out.println("给用户发送了通知");
    }

}
