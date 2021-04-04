package com.study.yygh.hosp.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.hosp.HospitalSet;
import com.study.yygh.vo.order.SignInfoVo;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-16 21:46
 * Versions:1.0.0
 * Description:
 */
public interface HospitalSetService extends IService<HospitalSet> {
    // 获取医院签名
    String getSignKey(String hoscode);

    // 获取医院签名信息
    SignInfoVo getSignInfoVo(String hoscode);
}
