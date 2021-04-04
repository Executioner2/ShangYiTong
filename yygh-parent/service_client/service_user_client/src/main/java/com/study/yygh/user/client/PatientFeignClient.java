package com.study.yygh.user.client;

import com.study.yygh.model.user.Patient;
import com.study.yygh.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 22:32
 * Versions:1.0.0
 * Description: 就诊人远程调用接口
 */
@FeignClient(value = "service-user")
@Component
public interface PatientFeignClient {
    /**
     * 根据id显示就诊人
     * @param id
     * @return
     */
    @GetMapping("/api/user/patient/inner/get/{id}")
    Patient getPatientOrder(@PathVariable("id") Long id);
}
