package com.study.yygh.hosp.controller;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.yygh.hosp.service.HospitalSetService;
import com.study.yygh.model.hosp.HospitalSet;
import com.study.yygh.result.Result;
import com.study.yygh.util.MD5;
import com.study.yygh.vo.hosp.HospitalSetQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-16 21:50
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "医院设置管理")
@RestController
@RequestMapping("/admin/hosp/hospitalSet")
//@CrossOrigin // 跨域 在网关中进行了统一的跨域配置
public class HospitalSetController {

    @Resource
    private HospitalSetService hospitalSetService;

    // 1 查询医院设置表所有信息
    @ApiOperation(value = "获取所有医院设置")
    @GetMapping("/findAll")
    public Result findAll(){
        List<HospitalSet> list = hospitalSetService.list();
        return Result.ok(list);
    }

    // 2 逻辑删除医院设置
    @ApiOperation(value = "逻辑删除医院设置")
    @DeleteMapping("{id}")
    public Result removeHospSet(@PathVariable Long id){
        boolean flag = hospitalSetService.removeById(id);
        if (flag){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    // 3 条件查询带分页
    @ApiOperation(value = "条件查询带分页")
    @PostMapping("/findPageHospSet/{current}/{limit}")
    public Result findPageHospSet(@PathVariable Long current,
                                  @PathVariable Long limit,
                                  @RequestBody(required = false) HospitalSetQueryVo hospitalSetQueryVo){
        // 分页
        Page<HospitalSet> page = new Page<>(current, limit);
        // 自定义的条件
        QueryWrapper<HospitalSet> wrapper = new QueryWrapper<>();
        String hosname = hospitalSetQueryVo.getHosname();
        String hoscode = hospitalSetQueryVo.getHoscode();
        if(!StringUtils.isEmpty(hosname)){
            wrapper.like("hosname", hosname);
        }
        if(!StringUtils.isEmpty(hoscode)){
            wrapper.like("hoscode", hoscode);
        }
        // 根据分页和自定义条件查询数据库
        Page<HospitalSet> hospitalSetPage = hospitalSetService.page(page, wrapper);
        // 返回结果
        return Result.ok(hospitalSetPage);
    }

    // 4 添加医院设置
    @ApiOperation(value = "添加医院设置")
    @PostMapping("/saveHospitalSet")
    public Result saveHospitalSet(@RequestBody HospitalSet hospitalSet){
        // 设置状态 1 可以使用， 0 不能使用
        hospitalSet.setStatus(1);
        // 签名密钥
        Random random = new Random();
        hospitalSet.setSignKey(MD5.encrypt(System.currentTimeMillis() + "" + random.nextInt(1000)));
        // 插入进数据库
        boolean flag = hospitalSetService.save(hospitalSet);

        if(flag){
            return Result.ok();
        }else{
            return Result.fail();
        }

    }

    // 5 根据id获取医院设置
    @ApiOperation(value = "根据id获取医院设置")
    @GetMapping("/getHospSet/{id}")
    public Result getHost(@PathVariable Long id){
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        return Result.ok(hospitalSet);
    }

    // 6 修改医院设置
    @ApiOperation(value = "修改医院设置")
    @PutMapping("/updateHospSet")
    public Result updateHospSet(@RequestBody HospitalSet hospitalSet){
        boolean flag = hospitalSetService.updateById(hospitalSet);
        if(flag){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    // 7 批量删除医院设置
    @ApiOperation(value = "批量删除医院设置")
    @DeleteMapping("/batchRemove")
    public Result batchRemove(@RequestBody List<Long> idList){
        hospitalSetService.removeByIds(idList);
        return Result.ok();
    }

    // 8 医院设置锁定和解锁
    @ApiOperation(value = "医院设置锁定和解锁")
    @PutMapping("/lockHospitalSet/{id}/{status}")
    public Result lockHospitalSet(@PathVariable Long id,
                                  @PathVariable Integer status){

        HospitalSet hospitalSet = hospitalSetService.getById(id);
        hospitalSet.setStatus(status);
        boolean flag = hospitalSetService.updateById(hospitalSet);

        if(flag){
            return Result.ok();
        }else{
            return Result.fail();
        }
    }

    // 9 发送签名秘钥
    @ApiOperation(value = "发送签名秘钥")
    @PutMapping("sendKey/{id}")
    public Result sendKey(@PathVariable Long id){
        /*try {
            int i = 10 / 0;
        }catch (Exception e){
            throw new YyghException("自定义异常执行了", 500);
        }*/
        HospitalSet hospitalSet = hospitalSetService.getById(id);
        String signKey = hospitalSet.getSignKey();
        String hoscode = hospitalSet.getHoscode();
        // TODO 发送短信
        return Result.ok();
    }

}
