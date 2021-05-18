package com.study.yygh.order.api;

import com.study.yygh.enums.PaymentTypeEnum;
import com.study.yygh.order.service.PaymentInfoService;
import com.study.yygh.order.service.WeiXinService;
import com.study.yygh.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 17:36
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "微信支付接口")
@RestController
@RequestMapping("/api/order/weixin")
public class WeiXinApiController {
    @Resource
    private WeiXinService weiXinService;
    @Resource
    private PaymentInfoService paymentInfoService;


    // 下单生成支付链接
    @ApiOperation(value = "下单生成支付链接")
    @GetMapping("/createNative/{orderId}")
    public Result createNative(@PathVariable Long orderId){
        Map map = weiXinService.createNative(orderId);
        return Result.ok(map);
    }

    // 轮询查看是否支付成功
    @ApiOperation(value = "轮询查看是否支付成功")
    @GetMapping("/queryPayStatus/{orderId}")
    public Result queryPayStatus(@PathVariable Long orderId){
        // 查询支付状态
        Map<String, String> map = weiXinService.queryPayStatus(orderId, PaymentTypeEnum.WEIXIN.name());
        if (map == null) {
            return Result.fail().message("支付失败");
        }
        System.out.println("支付状态：" + map.get("trade_state"));
        if ("SUCCESS".equals(map.get("trade_state"))){
            // 更改订单状态
            String outTradeNo = map.get("out_trade_no"); // 对外的订单号，微信支付结果上的商户单号
            paymentInfoService.paySuccess(outTradeNo, PaymentTypeEnum.WEIXIN.getStatus(), map);
            return Result.ok().message("支付成功");
        }
        return Result.ok().message("支付中");
    }
}
