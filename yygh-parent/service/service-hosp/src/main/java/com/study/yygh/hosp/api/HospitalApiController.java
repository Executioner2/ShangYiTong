package com.study.yygh.hosp.api;

import com.study.yygh.hosp.service.DepartmentService;
import com.study.yygh.hosp.service.HospitalService;
import com.study.yygh.hosp.service.HospitalSetService;
import com.study.yygh.hosp.service.ScheduleService;
import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.result.Result;
import com.study.yygh.vo.hosp.DepartmentVo;
import com.study.yygh.vo.hosp.HospitalQueryVo;
import com.study.yygh.vo.hosp.ScheduleOrderVo;
import com.study.yygh.vo.order.SignInfoVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-25 18:06
 * Versions:1.0.0
 * Description:
 */
@RestController
@Api(tags = "医院管理用户接口")
@RequestMapping("/api/hosp/hospital")
public class HospitalApiController {
    @Resource
    private HospitalService hospitalService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private HospitalSetService hospitalSetService;

    // 条件分页显示医院
    @ApiOperation(value = "条件分页显示医院")
    @GetMapping("/show/{page}/{limit}")
    public Result show(@PathVariable Integer page,
                       @PathVariable Integer limit,
                       HospitalQueryVo hospitalQueryVo){

        Page<Hospital> pageHospital = hospitalService.findPageHospital(page, limit, hospitalQueryVo);
        return Result.ok(pageHospital);
    }

    // 根据输入的不完全的医院名称联想出医院列表
    @ApiOperation(value = "医院名称联想")
    @GetMapping("/findByHosname/{hosname}")
    public Result findByHosname(@PathVariable String hosname){
        List<Hospital> list = hospitalService.findByHosname(hosname);
        return Result.ok(list);
    }

    // 根据hoscode获取医院预约挂号详情
    @ApiOperation(value = "医院预约挂号详情")
    @GetMapping("{hoscode}")
    public Result getHosp(@PathVariable String hoscode){
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        return Result.ok(hospital);
    }

    // 根据hoscode获取科室列表
    @ApiOperation(value = "获取科室列表")
    @GetMapping("/department/{hoscode}")
    public Result getDepartment(@PathVariable String hoscode){
        List<DepartmentVo> list = departmentService.findDeptTree(hoscode);
        return Result.ok(list);
    }

    // 获取可预约排班信息
    @ApiOperation(value = "获取可预约排班信息")
    @GetMapping("/auth/getBookingScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getBookingScheduleRule(@PathVariable Integer page,
                                         @PathVariable Integer limit,
                                         @PathVariable String hoscode,
                                         @PathVariable String depcode){
        Map<String, Object> map = scheduleService.getBookingScheduleRule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    // 获取排班信息
    @ApiOperation(value = "获取排班信息")
    @GetMapping("/auth/findScheduleList/{hoscode}/{depcode}/{workDate}")
    public Result findScheduleList(@PathVariable String hoscode,
                                   @PathVariable String depcode,
                                   @PathVariable String workDate){
        List<Schedule> scheduleDetails = scheduleService.getScheduleDetails(hoscode, depcode, workDate);
        return Result.ok(scheduleDetails);
    }

    // 根据排班id获取排班数据
    @ApiOperation(value = "根据排班id获取排班数据")
    @GetMapping("/getSchedule/{scheduleId}")
    public Result getSchedule(@PathVariable String scheduleId){
        Schedule schedule = scheduleService.getById(scheduleId);
        return Result.ok(schedule);
    }

    // 根据排班id获取预约下单数据
    @ApiOperation(value = "根据排班id获取预约下单数据")
    @GetMapping("/inner/getScheduleOrderVo/{scheduleId}")
    public ScheduleOrderVo getScheduleOrderVo(@PathVariable String scheduleId){
        ScheduleOrderVo scheduleOrderVo = scheduleService.getScheduleOrderVo(scheduleId);
        return scheduleOrderVo;
    }

    // 获取医院签名信息
    @ApiOperation(value = "获取医院签名信息")
    @GetMapping("/inner/getSignInfoVo/{hoscode}")
    public SignInfoVo getSignInfoVo(@PathVariable String hoscode){
        SignInfoVo signInfoVo = hospitalSetService.getSignInfoVo(hoscode);
        return signInfoVo;
    }
}
