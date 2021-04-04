package com.study.yygh.hosp.repository;

import com.study.yygh.model.hosp.Hospital;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 17:54
 * Versions:1.0.0
 * Description:
 */
@Repository
public interface HospitalRepository extends MongoRepository<Hospital, String> {
    // 根据医院编号查询出医院
    Hospital getHospitalByHoscode(String hoscode);

    // 根据输入的医院名称模糊匹配医院列表
    List<Hospital> findHospitalByHosnameLike(String hosname);
}
