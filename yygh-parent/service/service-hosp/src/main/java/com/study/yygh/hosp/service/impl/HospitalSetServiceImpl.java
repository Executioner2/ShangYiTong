package com.study.yygh.hosp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.yygh.exception.YyghException;
import com.study.yygh.hosp.mapper.HospitalSetMapper;
import com.study.yygh.hosp.service.HospitalSetService;
import com.study.yygh.model.hosp.HospitalSet;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.vo.order.SignInfoVo;
import org.springframework.stereotype.Service;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-16 21:47
 * Versions:1.0.0
 * Description:
 */
@Service
public class HospitalSetServiceImpl extends ServiceImpl<HospitalSetMapper, HospitalSet> implements HospitalSetService {
    /**
     * 根据hoscode查询hospital
     * @param hoscode
     * @return 返回查询到的hospital的signKey
     */
    @Override
    public String getSignKey(String hoscode) {
        QueryWrapper<HospitalSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(queryWrapper);
        return hospitalSet.getSignKey();
    }

    /**
     * 获取医院签名信息
     * @param hoscode
     * @return
     */
    @Override
    public SignInfoVo getSignInfoVo(String hoscode) {
        // 查出医院
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        wrapper.eq("hoscode", hoscode);
        HospitalSet hospitalSet = baseMapper.selectOne(wrapper);
        if (hospitalSet == null) {
            throw new YyghException(ResultCodeEnum.HOSPITAL_OPEN);
        }
        SignInfoVo signInfoVo = new SignInfoVo();
        signInfoVo.setApiUrl(hospitalSet.getApiUrl());
        signInfoVo.setSignKey(hospitalSet.getSignKey());
        return signInfoVo;
    }
}
