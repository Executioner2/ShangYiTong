package com.study.yygh.hosp.controller;

import com.study.yygh.hosp.service.ScheduleService;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.result.Result;
import com.study.yygh.hosp.service.DepartmentService;
import com.study.yygh.vo.hosp.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-24 17:54
 * Versions:1.0.0
 * Description:
 */
@RestController
@RequestMapping(value = "/admin/hosp/department")
@Api(tags = "医院排班管理")
//@CrossOrigin // 跨域 在网关中进行了统一的跨域配置
public class DepartmentController {
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ScheduleService scheduleService;

    // 根据医院编号，查询医院所有科室列表
    @ApiOperation(value = "查询医院所有科室列表")
    @GetMapping("/getDeptList/{hoscode}")
    public Result getDeptList(@PathVariable String hoscode) {
        // 查询医院所有科室列表，按树型显示
        List<DepartmentVo> departmentVos = departmentService.findDeptTree(hoscode);
        return Result.ok(departmentVos);
    }

    // 根据医院编号 和 科室编号，查询排班规则数据
    @ApiOperation(value = "查询排班规则数据")
    @GetMapping("/getScheduleRule/{page}/{limit}/{hoscode}/{depcode}")
    public Result getScheduleRule(@PathVariable Long page,
                                  @PathVariable Long limit,
                                  @PathVariable String hoscode,
                                  @PathVariable String depcode){
        Map<String, Object> map = scheduleService.getScheduleRule(page, limit, hoscode, depcode);
        return Result.ok(map);
    }

    // 根据 hoscode depcode 和 workDate 查询排班详情列表
    @ApiOperation(value = "查询排班详情列表")
    @GetMapping("/getScheduleDetails/{hoscode}/{depcode}/{workDate}")
    public Result getScheduleDetails(@PathVariable String hoscode,
                                     @PathVariable String depcode,
                                     @PathVariable String workDate){

        List<Schedule> list =  scheduleService.getScheduleDetails(hoscode, depcode, workDate);
        return Result.ok(list);
    }



}
