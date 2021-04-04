package com.study.yygh.hosp.service;

import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:00
 * Versions:1.0.0
 * Description:
 */
public interface HospitalService {
    // 上传医院
    void save(Map<String, Object> parameterMap);

    // 根据hoscode查询医院
    Hospital getByHoscode(String hoscode);

    // 分页显示医院
    Page<Hospital> findPageHospital(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo);

    // 修改医院上下线状态
    void updateStatus(String id, Integer status);

    // 根据id查询出医院详情
    Map<String, Object> showById(String id);

    // 获取医院名称
    String getHospName(String hoscode);

    // 根据输入的不完全的医院名称联想出医院列表
    List<Hospital> findByHosname(String hosname);
}
