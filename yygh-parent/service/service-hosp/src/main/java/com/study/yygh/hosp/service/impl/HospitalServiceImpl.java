package com.study.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.study.yygh.cmn.client.DictFeignClient;
import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.hosp.repository.HospitalRepository;
import com.study.yygh.hosp.service.HospitalService;
import com.study.yygh.vo.hosp.HospitalQueryVo;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:00
 * Versions:1.0.0
 * Description:
 */
@Service
public class HospitalServiceImpl implements HospitalService {
    @Resource
    private HospitalRepository hospitalRepository;
    @Resource
    private DictFeignClient dictFeignClient;

    /**
     * 上传医院，如果医院存在就更新
     * @param parameterMap
     */
    @Override
    public void save(Map<String, Object> parameterMap) {
        // 要把map转换为对象，要先把map转为字符串
        String jsonString = JSONObject.toJSONString(parameterMap);
        // 再把json字符串转换为对象
        Hospital hospital = JSONObject.parseObject(jsonString, Hospital.class);
        // 查询mongodb中存不存再此数据，不存在则添加，存在则更新
        // getHospitalByHoscode() 这个方法遵循Spring Data命名
        // 所以spring会自动帮忙实现添加了Repository注解的方法(通俗理解)
        Hospital hospitalExist = hospitalRepository.getHospitalByHoscode(hospital.getHoscode());

        if(hospitalExist != null){ // 如果不为空就更新
            hospital.setStatus(hospitalExist.getStatus());
            hospital.setCreateTime(hospitalExist.getCreateTime());
            hospital.setUpdateTime(new Date());
            hospital.setIsDeleted(0);
            hospitalRepository.save(hospital);
        }else{ // 否则保存此数据
            // 0： 未上线，  1： 已上线
            hospital.setStatus(0); // 第一次保存即未与预约挂号系统对接，则未上线
            hospital.setIsDeleted(0);
            hospital.setCreateTime(new Date());
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }

    }

    /**
     * 根据hoscode查询医院
     * @param hoscode
     * @return 医院
     */
    @Override
    public Hospital getByHoscode(String hoscode) {
        // 查询出医院并给他搞上等级
        return this.setHospitalType(hospitalRepository.getHospitalByHoscode(hoscode));
    }

    /**
     * 条件分页查询医院
     * @param page
     * @param limit
     * @param hospitalQueryVo
     * @return
     */
    @Override
    public Page<Hospital> findPageHospital(Integer page, Integer limit, HospitalQueryVo hospitalQueryVo) {
        // 分页设置
        Pageable pageable = PageRequest.of(page - 1, limit);
        // 把 hospitalQueryVo 的属性值复制给 hospital
        Hospital hospital = new Hospital();
        BeanUtils.copyProperties(hospitalQueryVo, hospital);
        // 设置匹配模式
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING) // 模糊查询
                .withIgnoreCase(true); // 不区分大小写
        Example<Hospital> example = Example.of(hospital, matcher);
        Page<Hospital> repositoryAll = hospitalRepository.findAll(example, pageable);
        // 对医院等级进行封装
        repositoryAll.getContent().stream().forEach(item -> {
            this.setHospitalType(item);
        });

        return repositoryAll;
    }

    /**
     * 修改医院上下线状态
     * @param id
     * @param status
     */
    @Override
    public void updateStatus(String id, Integer status) {
        // 判断status值是否合法
        // 0：未上线   1：已上线
        if(status.intValue() == 0 || status.intValue() == 1) {
            // 根据id查询出这个医院
            Hospital hospital = hospitalRepository.findById(id).get();
            // 更新这个医院
            hospital.setStatus(status);
            hospital.setUpdateTime(new Date());
            hospitalRepository.save(hospital);
        }
    }

    /**
     * 根据id查询出医院详情
     * @param id
     * @return
     */
    @Override
    public Map<String, Object> showById(String id) {
        // 因为医院等级信息需要调用到cmn模块
        // 所以这里用到了之前的 setHospitalType 方法
        Map<String, Object> map = new HashMap<>();
        Hospital hospital = this.setHospitalType(hospitalRepository.findById(id).get());
        // 为了前端显示方便，把 hospital和bookingRule 放到map集合中
        map.put("hospital", hospital);
        // 把预约规则单独拿出来，为了方便前端的展示
        map.put("bookingRule", hospital.getBookingRule());
        // 把hospital对象内原本的预约规则设置为null
        hospital.setBookingRule(null);

        return map;
    }

    /**
     * 获取医院名称
     * @param hoscode
     * @return
     */
    @Override
    public String getHospName(String hoscode) {
        // 使用spring data自动写匹配代码
        Hospital hospital = hospitalRepository.getHospitalByHoscode(hoscode);
        if (hospital != null) {
            return hospital.getHosname();
        }
        return null;
    }

    /**
     * 根据输入的不完全的医院名称联想出医院列表
     * @param hosname
     * @return
     */
    @Override
    public List<Hospital> findByHosname(String hosname) {
        List<Hospital> list = hospitalRepository.findHospitalByHosnameLike(hosname);
        return list;
    }


    /**
     * 设置医院等级信息
     * @param hospital
     * @return
     */
    private Hospital setHospitalType(Hospital hospital){
        // 调用远程cmn模块，查询医院等级
        String dictName = dictFeignClient.getName("Hostype",hospital.getHostype());

        // 测试获取 省  市  地区
        String province = dictFeignClient.getName(hospital.getProvinceCode());
        String city = dictFeignClient.getName(hospital.getCityCode());
        String district = dictFeignClient.getName(hospital.getDistrictCode());

        hospital.getParam().put("hostypeString", dictName);
        hospital.getParam().put("fullAddress", province + city + district);
        return hospital;
    }
}
