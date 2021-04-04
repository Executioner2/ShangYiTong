package com.study.yygh.hosp.repository;

import com.study.yygh.model.hosp.Department;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 17:59
 * Versions:1.0.0
 * Description:
 */
@Repository
public interface DepartmentRepository extends MongoRepository<Department, String> {
    Department getDepartmentByHoscodeAndDepcode(String hoscode, String depcode);
}
