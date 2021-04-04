package com.study.yygh.order.service;

import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 17:37
 * Versions:1.0.0
 * Description: 微信支付接口
 */
public interface WeiXinService {
    // 根据订单号，生成支付链接
    Map createNative(Long orderId);

    // 查询支付状态
    Map<String, String> queryPayStatus(Long orderId, String name);

    // 已支付 退款
    boolean refund(Long orderId);
}
