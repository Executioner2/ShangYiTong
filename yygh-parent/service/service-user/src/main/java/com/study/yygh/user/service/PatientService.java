package com.study.yygh.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.user.Patient;

import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 22:52
 * Versions:1.0.0
 * Description:
 */
public interface PatientService extends IService<Patient> {
    // 显示就诊人
    List<Patient> findAll(Long userId);

    // 根据id显示就诊人
    Patient getPatientById(Long id);
}
