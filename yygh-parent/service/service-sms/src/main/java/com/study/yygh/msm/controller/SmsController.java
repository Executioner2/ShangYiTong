package com.study.yygh.msm.controller;

import com.study.yygh.msm.service.SmsService;
import com.study.yygh.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 18:26
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "MSM短信管理")
@RequestMapping("/api/msm")
@RestController
public class SmsController {
    @Resource
    private SmsService smsService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // 发送短信
    @ApiOperation(value = "发送短信")
    @GetMapping("/send/{phone}")
    public Result send(@PathVariable String phone) {
        // 查询redis中是否存在code，避免重复发送
        String code = redisTemplate.opsForValue().get(phone);

        if (!StringUtils.isEmpty(code)) { // 如果不为空则返回
            return Result.ok();
        }

//###########################################################################
        // 由于未能在阿里云申请到短信服务，故把验证码写为固定值
        code = "123456";
        // 存入到redis数据库，设置有效时间为5分钟
        redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
        return Result.ok();
//###########################################################################


        // TODO 如果申请到了短信服务，就按以下逻辑写这此方法逻辑
//        // 用msm服务发送到用户手机上
//        // 随机生成一个6位的code
//        code = RandomUtil.getSixBitRandom();
//        boolean isSend = smsService.com.study.yygh.order.send(phone, code);
//        if (isSend) {
//            // 存入到redis数据库，设置有效时间为5分钟
//            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
//            return Result.ok();
//        } else {
//            return Result.fail().message("消息发送失败");
//        }
    }
}
