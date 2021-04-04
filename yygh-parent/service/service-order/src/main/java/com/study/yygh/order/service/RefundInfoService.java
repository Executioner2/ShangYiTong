package com.study.yygh.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.order.PaymentInfo;
import com.study.yygh.model.order.RefundInfo;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 19:24
 * Versions:1.0.0
 * Description:
 */
public interface RefundInfoService extends IService<RefundInfo> {
    // 保存退款记录
    RefundInfo saveRefundInfo(PaymentInfo paymentInfo);

}
