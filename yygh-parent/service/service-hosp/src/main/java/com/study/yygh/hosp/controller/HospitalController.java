package com.study.yygh.hosp.controller;

import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.result.Result;
import com.study.yygh.hosp.service.HospitalService;
import com.study.yygh.vo.hosp.HospitalQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-22 22:17
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "医院管理接口")
@RestController
@RequestMapping("/admin/hosp/hospital")
//@CrossOrigin // 跨域 在网关中进行了统一的跨域配置
public class HospitalController {
    @Resource
    private HospitalService hospitalService;

    // 获取分页列表
    @ApiOperation(value = "获取分页列表")
    @GetMapping("/list/{page}/{limit}")
    public Result findPageHospital(@PathVariable Integer page,
                                   @PathVariable Integer limit,
                                   HospitalQueryVo hospitalQueryVo){

        Page<Hospital> pageModel = hospitalService.findPageHospital(page, limit, hospitalQueryVo);
        return Result.ok(pageModel);
    }

    // 医院上线下线
    @ApiOperation(value = "医院上线下线")
    @PutMapping("/update/{id}/{status}")
    public Result updateStatus(@PathVariable String id,
                               @PathVariable Integer status){
        hospitalService.updateStatus(id, status);
        return Result.ok();
    }

    // 显示医院详情
    @ApiOperation(value = "显示医院详情")
    @GetMapping("/show/{id}")
    public Result showById(@PathVariable String id){
        Map<String, Object> map = hospitalService.showById(id);
        return Result.ok(map);
    }

}
