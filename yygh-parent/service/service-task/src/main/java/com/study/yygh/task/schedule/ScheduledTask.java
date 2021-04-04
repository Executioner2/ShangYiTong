package com.study.yygh.task.schedule;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-04 14:42
 * Versions:1.0.0
 * Description:
 */
@EnableScheduling // 开启定时任务
@Component
@EnableBinding(Source.class)
public class ScheduledTask {
    @Resource
    private MessageChannel output;

    /**
     * 定时发邮件给用户提醒有新的预约可申请
     * 0 0 8 * * ?   每天八点
     */
    @Scheduled(cron = "10 * * * * ?") // cron表达式，听说这个只支持6位
    public void task(){
        // 发送到mq，做测试就10秒发送一次
        output.send(MessageBuilder.withPayload("").build());
        System.out.println("定时向mq发送了消息");
    }
}
