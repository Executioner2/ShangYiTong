package com.study.yygh.order.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.yygh.enums.OrderStatusEnum;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.order.service.OrderService;
import com.study.yygh.result.Result;
import com.study.yygh.util.AutoContextHolder;
import com.study.yygh.vo.order.OrderCountQueryVo;
import com.study.yygh.vo.order.OrderQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 22:25
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "订单接口")
@RestController
@RequestMapping("/api/order/orderInfo")
public class OrderApiController {
    @Resource
    private OrderService orderService;

    // 创建订单
    @ApiOperation(value = "创建订单")
    @PostMapping("/auth/submitOrder/{scheduleId}/{patientId}")
    public Result submitOrder(@PathVariable String scheduleId,
                              @PathVariable Long patientId){
        Long orderId = orderService.saveOrder(scheduleId, patientId);
        return Result.ok(orderId);
    }

    // 订单列表（条件查询带分页）
    @ApiOperation(value = "订单列表（条件查询带分页）")
    @GetMapping("/auth/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       OrderQueryVo orderQueryVo,
                       HttpServletRequest request){
        // 设置用户id
        Long userId = AutoContextHolder.getUserId(request);
        orderQueryVo.setUserId(userId);
        Page<OrderInfo> infoPage = new Page<>(page, limit);
        IPage<OrderInfo> pageModel = orderService.selectPage(infoPage, orderQueryVo);
        return Result.ok(pageModel);
    }

    // 获取订单状态
    @ApiOperation(value = "获取订单状态")
    @GetMapping("/auth/getStatusList")
    public Result getStatusList(){
        // 返回订单状态枚举值集合
        return Result.ok(OrderStatusEnum.getStatusList());
    }

    // 查询订单
    @ApiOperation(value = "查询订单")
    @GetMapping("/auth/get/{orderId}")
    public Result getOrderById(@PathVariable Long orderId){
        OrderInfo orderInfo = orderService.getById(orderId);
        return Result.ok(orderInfo);
    }

    // 取消预约，已支付
    @ApiOperation(value = "取消预约，已支付")
    @GetMapping("/auth/cancelOrder/{orderId}")
    public Result cancelOrder(@PathVariable Long orderId){
        // 取消预约
        Boolean flag = orderService.cancelOrder(orderId);
        return Result.ok(flag);
    }

    // 获取订单统计数据
    @ApiOperation(value = "获取订单统计数据")
    @PostMapping("/inner/getCountMap")
    public Map<String, Object> getCountMap(@RequestBody OrderCountQueryVo orderCountQueryVo){
        Map<String, Object> result = orderService.getCountMap(orderCountQueryVo);
        return result;
    }
}

