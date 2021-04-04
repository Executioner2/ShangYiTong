package com.study.yygh.hosp.service;

import com.study.yygh.model.hosp.Department;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.vo.hosp.DepartmentVo;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:01
 * Versions:1.0.0
 * Description:
 */
public interface DepartmentService {
    // 上传科室
    void save(Map<String, Object> map);

    // 分页查询科室
    Page<Department> findPage(int page, int limit, String hoscode);

    // 删除科室
    void remove(String hoscode, String depcode);

    // 根据医院编号，查询医院所有科室列表
    List<DepartmentVo> findDeptTree(String hoscode);

    // 根据 hoscode 和 depcode 获取科室
    Department getDepartment(String hoscode, String depcode);

    // 设置科室名称
    String getDepName(String hoscode, String depcode);
}
