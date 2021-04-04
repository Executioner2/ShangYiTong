package com.study.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.model.order.PaymentInfo;

import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 17:44
 * Versions:1.0.0
 * Description:
 */
public interface PaymentInfoService extends IService<PaymentInfo> {
    // 保存交易记录
    void savePaymentInfo(OrderInfo orderInfo, Integer paymentType);

    // 更改订单状态
    void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> map);

    // 获得支付记录
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
