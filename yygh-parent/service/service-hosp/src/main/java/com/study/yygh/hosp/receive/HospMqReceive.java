package com.study.yygh.hosp.receive;

import com.alibaba.fastjson.JSON;
import com.study.yygh.exception.YyghException;
import com.study.yygh.hosp.service.ScheduleService;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.result.ResultCodeEnum;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.messaging.Message;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 15:44
 * Versions:1.0.0
 * Description: Hosp模块mq接收者
 */
@EnableBinding(Sink.class)
public class HospMqReceive {
    @Resource
    private ScheduleService scheduleService;

    /**
     * 号源更新
     * @param message
     */
    @StreamListener(Sink.INPUT)
    public void availableNumberUpdate(Message<String> message){
        // 取得变成字符串的OrderMqVo
        String payload = message.getPayload();
        // 如果字符串为空则抛出数据异常
        if (StringUtils.isEmpty(payload)){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        // 把字符串转换为HashMap，不转换为OrderMqVo是怕传来的不是OrderMqVo，转换为map取key最保险
        HashMap<String, Object> hashMap = JSON.parseObject(payload, HashMap.class);
        Integer reservedNumber = (Integer) hashMap.get("reservedNumber");
        if (reservedNumber == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        Integer availableNumber = (Integer) hashMap.get("availableNumber");
        if (availableNumber == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        String scheduleId = (String) hashMap.get("scheduleId");
        if (StringUtils.isEmpty(scheduleId)){
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        Schedule schedule = scheduleService.getScheduleId(scheduleId);
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        schedule.setAvailableNumber(availableNumber - 1); // 剩余预约数-1
        schedule.setReservedNumber(reservedNumber + 1); // 预约数+1
        // 更新数据库
        scheduleService.update(schedule);
    }
}
