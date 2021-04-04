package com.study.yygh.order.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.vo.order.OrderCountQueryVo;
import com.study.yygh.vo.order.OrderQueryVo;

import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 22:23
 * Versions:1.0.0
 * Description:
 */
public interface OrderService extends IService<OrderInfo> {
    // 创建订单
    Long saveOrder(String scheduleId, Long patientId);

    // 订单列表（条件查询带分页）
    IPage<OrderInfo> selectPage(Page<OrderInfo> infoPage, OrderQueryVo orderQueryVo);

    // 取消预约
    Boolean cancelOrder(Long orderId);

    // 获取订单统计数据
    Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo);
}
