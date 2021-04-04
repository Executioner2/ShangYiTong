package com.study.yygh.hosp.client;

import com.study.yygh.vo.hosp.ScheduleOrderVo;
import com.study.yygh.vo.order.SignInfoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-01 17:13
 * Versions:1.0.0
 * Description:
 */
@Component
@FeignClient(value = "service-hosp")
public interface HospitalFeignClient {
    // 获取医院签名信息
    @GetMapping("/api/hosp/hospital/inner/getSignInfoVo/{hoscode}")
    SignInfoVo getSignInfoVo(@PathVariable("hoscode") String hoscode);

    // 根据排班id获取预约下单数据
    @GetMapping("/api/hosp/hospital/inner/getScheduleOrderVo/{scheduleId}")
    ScheduleOrderVo getScheduleOrderVo(@PathVariable("scheduleId") String scheduleId);
}
