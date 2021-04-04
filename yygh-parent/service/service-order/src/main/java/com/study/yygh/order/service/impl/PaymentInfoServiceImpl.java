package com.study.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.yygh.enums.PaymentStatusEnum;
import com.study.yygh.exception.YyghException;
import com.study.yygh.helper.HttpRequestHelper;
import com.study.yygh.hosp.client.HospitalFeignClient;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.model.order.PaymentInfo;
import com.study.yygh.order.mapper.PaymentInfoMapper;
import com.study.yygh.order.service.OrderService;
import com.study.yygh.order.service.PaymentInfoService;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.vo.order.SignInfoVo;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 17:45
 * Versions:1.0.0
 * Description:
 */
@Service
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {
    @Resource
    private OrderService orderService;
    @Resource
    private HospitalFeignClient hospitalFeignClient;

    /**
     * 保存交易记录
     * @param orderInfo
     * @param paymentType 支付类型（1：微信  2：支付宝）
     */
    @Override
    public void savePaymentInfo(OrderInfo orderInfo, Integer paymentType) {
        // 先查询数据库中有没有交易记录，有就不用保存
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderInfo.getId());
        wrapper.eq("payment_type", paymentType);
        Integer integer = baseMapper.selectCount(wrapper);
        if (integer > 0) return; // 有交易记录，直接返回

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus()); // 交易状态
        String subject = new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd")+"|"+orderInfo.getHosname()+"|"+orderInfo.getDepname()+"|"+orderInfo.getTitle();
        paymentInfo.setSubject(subject); // 交易内容
        paymentInfo.setTotalAmount(orderInfo.getAmount()); // 支付金额
        baseMapper.insert(paymentInfo); // 保存到数据库
    }

    /**
     * 更改订单状态
     * @param outTradeNo
     * @param paymentType
     * @param map
     */
    @Override
    public void paySuccess(String outTradeNo, Integer paymentType, Map<String, String> map) {
        // 查询订单
        PaymentInfo paymentInfo = this.getPaymentInfo(outTradeNo, paymentType);
        if (paymentInfo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if (paymentInfo.getPaymentStatus() != PaymentStatusEnum.UNPAID.getStatus()) return;

        // 修改支付状态
        paymentInfo.setPaymentStatus(PaymentStatusEnum.PAID.getStatus());
        paymentInfo.setUpdateTime(new Date());
        paymentInfo.setTradeNo(map.get("transaction_id")); // 微信那边的订单号
        paymentInfo.setCallbackTime(new Date()); // 回调时间
        paymentInfo.setCallbackContent(map.toString()); // 回调信息，就是微信返回的那个参数
        // 根据对外编号和支付类型来保存（传来的参数没有唯一id）
        this.updatePaymentInfo(outTradeNo, paymentInfo);

        // 修改订单状态
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        orderInfo.setOrderStatus(PaymentStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);
        // 调用医院接口，通知更新状态
        SignInfoVo signInfoVo
                = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if(null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);
        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updatePayStatus");
        if(result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
    }

    /**
     * 获得支付记录
     * @param orderId
     * @param paymentType
     * @return
     */
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId);
        queryWrapper.eq("payment_type", paymentType);
        return baseMapper.selectOne(queryWrapper);
    }

    // 根据对外编号和支付类型来保存
    private void updatePaymentInfo(String outTradeNo, PaymentInfo paymentInfo) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", outTradeNo);
        wrapper.eq("payment_type", paymentInfo.getPaymentType());
        this.update(paymentInfo, wrapper);
    }

    // 查询订单
    private PaymentInfo getPaymentInfo(String outTradeNo, Integer paymentType) {
        QueryWrapper<PaymentInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("out_trade_no", outTradeNo);
        wrapper.eq("payment_type", paymentType);
        PaymentInfo paymentInfo = baseMapper.selectOne(wrapper);
        return paymentInfo;
    }
}
