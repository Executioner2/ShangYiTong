package com.study.yygh.hosp.util;

import com.alibaba.excel.util.StringUtils;
import com.study.yygh.exception.YyghException;
import com.study.yygh.helper.HttpRequestHelper;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.hosp.service.HospitalSetService;
import com.study.yygh.util.MD5;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-22 16:38
 * Versions:1.0.0
 * Description:
 */
@Component
public class ApiUtil {
    @Resource
    private HospitalSetService hospitalSetService;

    public void signKey(Map<String, Object> map) {
        String hoscode = (String) map.get("hoscode");
        // 1 判断传来的参数中是否有hoscode
        if(StringUtils.isEmpty(hoscode)){ // 如果传来的hoscode为空则抛出异常
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 2 根据hoscode查询数据库中hospital记录的签名
        String signKey = hospitalSetService.getSignKey(hoscode);
        // 3 获得MD5加密后的字符串
        signKey = MD5.encrypt(signKey);
        // 4 进行签名验证
        if(!HttpRequestHelper.isSignEquals(map, signKey)){ // 如果不相等则抛出异常
            throw  new YyghException(ResultCodeEnum.SIGN_ERROR);
        }
    }

    public void base64Image(Map<String, Object> map){
        String logoData = (String) map.get("logoData");
        logoData = logoData.replaceAll(" ", "+");
        map.put("logoData", logoData);
    }
}
