package com.study.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.study.yygh.exception.YyghException;
import com.study.yygh.hosp.service.DepartmentService;
import com.study.yygh.model.hosp.BookingRule;
import com.study.yygh.model.hosp.Department;
import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.hosp.repository.DepartmentRepository;
import com.study.yygh.hosp.repository.ScheduleRepository;
import com.study.yygh.hosp.service.HospitalService;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.vo.hosp.BookingScheduleRuleVo;
import com.study.yygh.vo.hosp.DepartmentVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:01
 * Versions:1.0.0
 * Description:
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    @Resource
    private DepartmentRepository departmentRepository;

    /**
     * 保存科室，根据mongoDB中是否已存在数据来
     * 执行更新还是保存
     * @param map
     */
    @Override
    public void save(Map<String, Object> map) {
        // 把map对象转换为obj子类对象
        String jsonString = JSONObject.toJSONString(map);
        Department department = JSONObject.parseObject(jsonString, Department.class);

        // 根据hosoce和depcode取出对象
        Department departmentExist = departmentRepository.getDepartmentByHoscodeAndDepcode(department.getHoscode(), department.getDepcode());

        if(null != departmentExist){ // 不为null则是更新
            departmentExist.setUpdateTime(new Date());
            departmentExist.setIsDeleted(0);
            departmentRepository.save(departmentExist);
        }else{ // 否则是保存
            department.setCreateTime(new Date());
            department.setUpdateTime(new Date());
            department.setIsDeleted(0);
            departmentRepository.save(department);
        }

    }

    /**
     * 分页查询科室
     * @param page
     * @param limit
     * @param hoscode
     * @return
     */
    @Override
    public Page<Department> findPage(int page, int limit, String hoscode) {
        // 创建Pageable对象，设置当前页和每页大小
        Pageable pageable = PageRequest.of(page - 1, limit);
        // 创建Example对象
        Department department = new Department();
        department.setHoscode(hoscode);
        department.setIsDeleted(0);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // 改变字符串查询方式为模糊查询
                .withIgnoreCase(true); // 忽略大小写
        Example<Department> example = Example.of(department, exampleMatcher);

        Page<Department> all = departmentRepository.findAll(example, pageable);
        return all;
    }

    /**
     * 删除科室
     * @param hoscode
     * @param depcode
     */
    @Override
    public void remove(String hoscode, String depcode) {
        // 根据hoscode 和 depcode 查询Department
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if(null != department){ // 如果不为空，则根据id进行删除
            departmentRepository.deleteById(department.getId());
        }
    }

    /**
     * 根据医院编号，查询医院所有科室列表
     * @param hoscode
     * @return
     */
    @Override
    public List<DepartmentVo> findDeptTree(String hoscode) {
        // 最终返回结果
        List<DepartmentVo> result = new ArrayList<>();

        // 查询出所有的科室
        Department probe = new Department();
        probe.setHoscode(hoscode); // 查询条件
        Example example = Example.of(probe);
        List<Department> all = departmentRepository.findAll(example);

        // 对查询出来的科室按bigCode进行stream分组
        Map<String, List<Department>> collect = all.stream().collect(Collectors.groupingBy(Department::getBigcode));
        // 对collect进行遍历
        Iterator<String> iterable = collect.keySet().iterator();
        while(iterable.hasNext()){
            // 拿到 collect 中的key
            String key = iterable.next();
            // 根据 key 拿到 collect 中对应的value
            List<Department> value = collect.get(key);
            // 封装一级科室
            DepartmentVo parentDep = new DepartmentVo();
            parentDep.setDepcode(value.get(0).getBigcode());
            parentDep.setDepname(value.get(0).getBigname());
            // 封装二级科室
            List<DepartmentVo> childrenDepList = new ArrayList<>();
            for (Department d : value) {
                DepartmentVo childrenDep = new DepartmentVo();
                childrenDep.setDepname(d.getDepname());
                childrenDep.setDepcode(d.getDepcode());
                childrenDepList.add(childrenDep);
            }
            // 把二级科室封装到一级科室中去
            parentDep.setChildren(childrenDepList);
            // 把一级科室封装到结果中去
            result.add(parentDep);

        }
        return result;
    }

    /**
     * 根据 hoscode 和 depcode 获取科室
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Department getDepartment(String hoscode, String depcode) {
        return departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
    }

    /**
     * 设置科室名称
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public String getDepName(String hoscode, String depcode) {
        Department department = departmentRepository.getDepartmentByHoscodeAndDepcode(hoscode, depcode);
        if (department == null) {
            return "";
        }
        return department.getDepname();
    }

}
