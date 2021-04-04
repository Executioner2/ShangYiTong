package com.study.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import com.study.yygh.enums.PaymentTypeEnum;
import com.study.yygh.enums.RefundStatusEnum;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.model.order.PaymentInfo;
import com.study.yygh.model.order.RefundInfo;
import com.study.yygh.order.service.OrderService;
import com.study.yygh.order.service.PaymentInfoService;
import com.study.yygh.order.service.RefundInfoService;
import com.study.yygh.order.service.WeiXinService;
import com.study.yygh.order.util.ConstantPropertiesUtils;
import com.study.yygh.order.util.HttpClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-04-03 17:37
 * Versions:1.0.0
 * Description:
 */
@Service
public class WeiXinServiceImpl implements WeiXinService {
    @Resource
    private PaymentInfoService paymentInfoService;
    @Resource
    private OrderService orderService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RefundInfoService refundInfoService;

    /**
     * 根据订单号，生成支付链接
     * @param orderId
     * @return
     */
    @Override
    public Map createNative(Long orderId) {
        try {
            // 查询redis中有无有支付连接
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (payMap != null) { // 如果不为空就直接返回
                return payMap;
            }
            // 根据订单号获得订单
            OrderInfo orderInfo = orderService.getById(orderId);
            // 保存交易记录
            paymentInfoService.savePaymentInfo(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());
            // 下面是固定写法
            // 1、设置参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            String body = orderInfo.getReserveDate() + "就诊"+ orderInfo.getDepname();
            paramMap.put("body", body);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());
            //paramMap.put("total_fee", order.getAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee", "1"); // 做测试，设置为1分钱
            paramMap.put("spbill_create_ip", "127.0.0.1");
            paramMap.put("notify_url", "http://guli.shop/api/order/weixinPay/weixinNotify");
            paramMap.put("trade_type", "NATIVE");
            // 2、HttpClient根据url访问第三方接口并传递参数
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            // 设置参数（固定写法）
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            httpClient.setHttps(true); // 支持https
            httpClient.post(); // 发送post请求，执行这个方法就发送过去了
            // 3、获得返回信息，是xml格式的
            String xml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml); // 把xml转为map
            // 4、封装返回结果集
            Map map  = new HashMap();
            map.put("orderId", orderId);
            map.put("totalFee", orderInfo.getAmount());
            map.put("resultCode", resultMap.get("result_code"));
            map.put("codeUrl", resultMap.get("code_url"));
            if(StringUtils.isEmpty(resultMap.get("result_code"))) {
                // 微信支付二维码2小时过期，可采取2小时未支付取消订单
                redisTemplate.opsForValue().set(orderId.toString(), map, 1000, TimeUnit.MINUTES);
            }
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 查询支付状态
     * @param orderId
     * @param name
     * @return
     */
    @Override
    public Map<String, String> queryPayStatus(Long orderId, String name) {
        try {
            // 查询数据库
            OrderInfo orderInfo = orderService.getById(orderId);
            // 封装参数
            Map paramMap = new HashMap();
            paramMap.put("appid", ConstantPropertiesUtils.APPID);
            paramMap.put("mch_id", ConstantPropertiesUtils.PARTNER);
            paramMap.put("out_trade_no", orderInfo.getOutTradeNo());  // 对外的订单号
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(WXPayUtil.generateSignedXml(paramMap, ConstantPropertiesUtils.PARTNERKEY));
            httpClient.post();
            String xml = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 已支付 退款
     * @param orderId
     * @return
     */
    @Override
    public boolean refund(Long orderId) {
        try {
            PaymentInfo paymentInfoQuery = paymentInfoService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());

            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfoQuery);
            if(refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            Map<String,String> paramMap = new HashMap<>(8);
            paramMap.put("appid",ConstantPropertiesUtils.APPID);       //公众账号ID
            paramMap.put("mch_id",ConstantPropertiesUtils.PARTNER);   //商户编号
            paramMap.put("nonce_str",WXPayUtil.generateNonceStr());
            paramMap.put("transaction_id",paymentInfoQuery.getTradeNo()); //微信订单号
            paramMap.put("out_trade_no",paymentInfoQuery.getOutTradeNo()); //商户订单编号
            paramMap.put("out_refund_no","tk"+paymentInfoQuery.getOutTradeNo()); //商户退款单号
//       paramMap.put("total_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
//       paramMap.put("refund_fee",paymentInfoQuery.getTotalAmount().multiply(new BigDecimal("100")).longValue()+"");
            paramMap.put("total_fee","1");
            paramMap.put("refund_fee","1");
            String paramXml = WXPayUtil.generateSignedXml(paramMap,ConstantPropertiesUtils.PARTNERKEY);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/secapi/pay/refund");
            client.setXmlParam(paramXml);
            client.setHttps(true);
            client.setCert(true);
            client.setCertPassword(ConstantPropertiesUtils.PARTNER);
            client.post();
            //3、返回第三方的数据
            String xml = client.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(xml);
            if (null != resultMap && WXPayConstants.SUCCESS.equalsIgnoreCase(resultMap.get("result_code"))) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(resultMap.get("refund_id"));
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(resultMap));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
