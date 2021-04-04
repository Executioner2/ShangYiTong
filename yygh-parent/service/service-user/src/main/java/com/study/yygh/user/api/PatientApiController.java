package com.study.yygh.user.api;

import com.study.yygh.model.user.Patient;
import com.study.yygh.result.Result;
import com.study.yygh.user.service.PatientService;
import com.study.yygh.util.AutoContextHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 22:55
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "就诊人管理")
@RequestMapping("/api/user/patient")
@RestController
public class PatientApiController {
    @Resource
    PatientService patientService;

    // 显示所有就诊人
    @ApiOperation(value = "显示就诊人")
    @GetMapping("/auth/findAll")
    public Result findAll(HttpServletRequest request){
        Long userId = AutoContextHolder.getUserId(request);
        List<Patient> list = patientService.findAll(userId);
        return Result.ok(list);
    }

    // 根据id显示就诊人
    @ApiOperation(value = "根据id显示就诊人")
    @GetMapping("/auth/get/{id}")
    public Result getById(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return Result.ok(patient);
    }

    // 根据id显示就诊人
    @ApiOperation(value = "根据id显示就诊人（内部调用）")
    @GetMapping("/inner/get/{id}") // 这个和上面有一点不一样，这个是拱内部调用的接口，inner标志着内部调用
    public Patient getPatientOrder(@PathVariable Long id){
        Patient patient = patientService.getPatientById(id);
        return patient;
    }

    // 添加就诊人
    @ApiOperation(value = "添加就诊人")
    @PostMapping("/auth/save")
    public Result save(@RequestBody Patient patient, HttpServletRequest request){
        Long userId = AutoContextHolder.getUserId(request);
        patient.setUserId(userId);
        patientService.save(patient);
        return Result.ok();
    }

    // 删除就诊人
    @ApiOperation(value = "删除就诊人")
    @DeleteMapping("/auth/remove/{id}")
    public Result remove(@PathVariable Long id){
        patientService.removeById(id);
        return Result.ok();
    }


    // 更新就诊人
    @ApiOperation(value = "更新就诊人")
    @PutMapping("/auth/update")
    public Result update(@RequestBody Patient patient){
        patientService.updateById(patient);
        return Result.ok();
    }
}
