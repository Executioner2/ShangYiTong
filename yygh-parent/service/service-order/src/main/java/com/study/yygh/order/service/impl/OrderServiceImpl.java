package com.study.yygh.order.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.yygh.enums.OrderStatusEnum;
import com.study.yygh.exception.YyghException;
import com.study.yygh.helper.HttpRequestHelper;
import com.study.yygh.hosp.client.HospitalFeignClient;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.model.user.Patient;
import com.study.yygh.order.mapper.OrderInfoMapper;
import com.study.yygh.order.service.OrderService;
import com.study.yygh.order.service.WeiXinService;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.user.client.PatientFeignClient;
import com.study.yygh.vo.hosp.ScheduleOrderVo;
import com.study.yygh.vo.order.*;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 22:23
 * Versions:1.0.0
 * Description:
 */
@Service
@EnableBinding(Source.class)
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {
    @Resource
    private PatientFeignClient patientFeignClient;
    @Resource
    private HospitalFeignClient hospitalFeignClient;
    @Resource
    private MessageChannel output; // 消息发送信道，名字不能乱取，与application配置文件中的通道名字一致
    @Resource
    private WeiXinService weiXinService;

    /**
     * 创建订单
     * @param scheduleId
     * @param patientId
     * @return
     */
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        // 用openFeign远程调用user模块 获取就诊人信息
        Patient patient = patientFeignClient.getPatientOrder(patientId);
        if (patient == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 获得排班订单信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if (scheduleOrderVo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        if(scheduleOrderVo.getAvailableNumber() <= 0){
            throw new YyghException(ResultCodeEnum.NUMBER_NO);
        }
        //判断当前时间是否还可以预约
        if(new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }
        // 获得签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());
        if (signInfoVo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        OrderInfo orderInfo = new OrderInfo();
        BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
        // 设置订单交易号
        String outTradeNo = System.currentTimeMillis() + "" + RandomUtil.randomInt(100);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setScheduleId(scheduleId);
        orderInfo.setUserId(patient.getUserId());
        orderInfo.setPatientId(patientId);
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus()); // 设置订单状态
        // 保存到数据库中去
        baseMapper.insert(orderInfo);

        // 把参数写入一个map中，然后发给医院的系统，在医院那边实现下单
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",orderInfo.getHoscode());
        paramMap.put("depcode",orderInfo.getDepcode());
        paramMap.put("hosScheduleId",orderInfo.getScheduleId());
        paramMap.put("reserveDate",new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", orderInfo.getReserveTime());
        paramMap.put("amount",orderInfo.getAmount());
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        // 联系人
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
        paramMap.put("sign", sign);
        // 发送给医院的系统
        JSONObject result = HttpRequestHelper.sendRequest(paramMap, signInfoVo.getApiUrl()+"/order/submitOrder");
        // 如果成功在医院那边的系统中下单成功则返回信息，并更新预约挂号平台中的订单信息
        if(result.getInteger("code") == 200) {
            JSONObject jsonObject = result.getJSONObject("data");
            // 预约记录唯一标识（医院预约记录主键）
            String hosRecordId = jsonObject.getString("hosRecordId");
            // 预约序号
            Integer number = jsonObject.getInteger("number");;
            // 取号时间
            String fetchTime = jsonObject.getString("fetchTime");;
            // 取号地址
            String fetchAddress = jsonObject.getString("fetchAddress");;
            // 更新订单
            orderInfo.setHosRecordId(hosRecordId);
            orderInfo.setNumber(number);
            orderInfo.setFetchTime(fetchTime);
            orderInfo.setFetchAddress(fetchAddress);
            baseMapper.updateById(orderInfo);
            // 排班可预约数
            Integer reservedNumber = jsonObject.getInteger("reservedNumber");
            // 排班剩余预约数
            Integer availableNumber = jsonObject.getInteger("availableNumber");
            // TODO 发送mq信息更新号源和短信通知，因为sms要企业认证或有上线应用，所以后续改成邮件发送
            // 设置可预约数，然后利用stream(mq)发送消息到队列
            OrderMqVo orderMqVo = new OrderMqVo();
            orderMqVo.setAvailableNumber(availableNumber);
            orderMqVo.setReservedNumber(reservedNumber);
            orderMqVo.setScheduleId(scheduleId);
            // 发送消息到mq队列
            output.send(MessageBuilder.withPayload(orderMqVo).build());
        } else {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    /**
     * 订单列表（条件查询带分页）
     * @param infoPage
     * @param orderQueryVo
     * @return
     */
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> infoPage, OrderQueryVo orderQueryVo) {
        String createTimeBegin = orderQueryVo.getCreateTimeBegin(); // 创建日期结束
        String createTimeEnd = orderQueryVo.getCreateTimeEnd(); // 创建日期开始
        String keyword = orderQueryVo.getKeyword(); // 医院名称
        Long patientId = orderQueryVo.getPatientId(); // 就诊人名称
        String reserveDate = orderQueryVo.getReserveDate(); // 安排时间
        String orderStatus = orderQueryVo.getOrderStatus(); // 订单状态
        Long userId = orderQueryVo.getUserId(); // 用户ID
        // 对查询条件进行非空判断
        QueryWrapper<OrderInfo> wrapper = new QueryWrapper<>();
        // 查询就诊人信息
        if (!StringUtils.isEmpty(patientId)) {
            wrapper.eq("patient_id", patientId);
        }
        if (!StringUtils.isEmpty(keyword)) {
            wrapper.like("hosname", keyword);
        }
        if (!StringUtils.isEmpty(reserveDate)){
            wrapper.ge("reserve_date", reserveDate);
        }
        if (!StringUtils.isEmpty(createTimeBegin)) {
            wrapper.ge("create_time", createTimeBegin);
        }
        if (!StringUtils.isEmpty(createTimeEnd)) {
            wrapper.le("create_time", createTimeEnd);
        }
        if (!StringUtils.isEmpty(orderStatus)){
            wrapper.eq("order_status", orderStatus);
        }
        wrapper.eq("user_id", userId);
        // 根据条件查询数据库
        Page<OrderInfo> orderInfoPage = baseMapper.selectPage(infoPage, wrapper);
        // 把编号变成对应的值
        orderInfoPage.getRecords().stream().forEach(item -> {
            // 对订单状态编号进行包装
            this.orderInfoPackage(item);
        });
        return orderInfoPage;
    }

    /**
     * 取消预约
     * @param orderId
     * @return
     */
    @Override
    public Boolean cancelOrder(Long orderId) {
        // 查询数据库
        OrderInfo orderInfo = this.getById(orderId);
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        // 当前时间大于取消预约的时间不能取消
        if (quitTime.isBeforeNow()){
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        // 获得签名信息
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (signInfoVo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 医院接口修改订单信息
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode",orderInfo.getHoscode());
        reqMap.put("hosRecordId",orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl()+"/order/updateCancelStatus");
        if (result.getInteger("code") != 200){
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        } else {
            // 是否支付 退款
            if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
                // 已支付 退款
                boolean isRefund = weiXinService.refund(orderId);
                if (!isRefund) {
                    throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
                }
            }
        }
        // 更改订单状态
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        this.updateById(orderInfo);
        // TODO 短信通知
        return true;
    }

    /**
     * 获取订单统计数据
     * @param orderCountQueryVo
     * @return
     */
    @Override
    public Map<String, Object> getCountMap(OrderCountQueryVo orderCountQueryVo) {
        Map<String, Object> result = new HashMap<>();
        // 获得统计数据
        List<OrderCountVo> orderCountVos = baseMapper.selectOrderCount(orderCountQueryVo);
        // 把统计数据中的日期和统计数量拆分成两个list集合
        List<String> dateList = orderCountVos.stream().map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        List<Integer> countList = orderCountVos.stream().map(OrderCountVo::getCount).collect(Collectors.toList());
        // 存入到map中，方便前端展示
        result.put("dateList", dateList);
        result.put("countList", countList);
        return result;
    }

    // 对订单状态编号进行包装
    private OrderInfo orderInfoPackage(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString", OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
