package com.study.yygh.hosp.api;

import com.study.yygh.helper.HttpRequestHelper;
import com.study.yygh.hosp.service.DepartmentService;
import com.study.yygh.hosp.service.ScheduleService;
import com.study.yygh.model.hosp.Department;
import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.result.Result;
import com.study.yygh.hosp.service.HospitalService;
import com.study.yygh.hosp.util.ApiUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:06
 * Versions:1.0.0
 * Description: 对接hospital-manage系统的controller
 */
@RestController
@RequestMapping(value = "/api/hosp")
@Api(tags = "医院管理API接口")
public class ApiController {
    @Resource
    private ScheduleService scheduleService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private HospitalService hospitalService;
    @Resource
    private ApiUtil apiUtil;

    // 删除排班
    @ApiOperation(value = "删除排班")
    @PostMapping("/schedule/remove")
    public Result scheduleRemove(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        System.out.println(map);
        // 签名验证
        apiUtil.signKey(map);
        String hoscode = (String) map.get("hoscode");
        String hosScheduleId = (String) map.get("hosScheduleId");
        scheduleService.remove(hoscode, hosScheduleId);

        return Result.ok();
    }

    // 分页显示排班
    @ApiOperation(value = "分页显示排班")
    @PostMapping("/schedule/list")
    public Result scheduleList(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 签名验证
        apiUtil.signKey(map);

        // 获取page和limit
        int page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.valueOf((String)map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit")) ? 1 : Integer.valueOf((String)map.get("limit"));
        String hoscode = (String) map.get("hoscode");
        Page<Schedule> pageModel = scheduleService.findPage(page, limit, hoscode);

        return Result.ok(pageModel);
    }

    // 上传排班
    @ApiOperation(value = "上传排班")
    @PostMapping("/saveSchedule")
    public Result saveSchedule(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 签名验证
        apiUtil.signKey(map);

        scheduleService.save(map);

        return Result.ok();
    }

    // 删除科室
    @ApiOperation(value = "删除科室")
    @PostMapping("/department/remove")
    public Result departmentRemove(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 签名验证
        apiUtil.signKey(map);

        departmentService.remove((String)map.get("hoscode"), (String)map.get("depcode"));

        return Result.ok();
    }

    // 分页显示科室
    @ApiOperation(value = "分页显示科室")
    @PostMapping("/department/list")
    public Result departmentList(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 签名验证
        apiUtil.signKey(map);

        String hosoce = (String) map.get("hosoce");
        // 当前页和每页大小
        int page = StringUtils.isEmpty(map.get("page")) ? 1 : Integer.valueOf((String)map.get("page"));
        int limit = StringUtils.isEmpty(map.get("limit")) ? 1 : Integer.valueOf((String)map.get("limit"));

        // 调用service方法
        Page<Department> pageModel =  departmentService.findPage(page, limit, hosoce);

        return Result.ok(pageModel);
    }


    // 上传科室
    @ApiOperation(value = "上传科室")
    @PostMapping("/saveDepartment")
    public Result saveDepartment(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 签名验证
        apiUtil.signKey(map);

        // 调用上传科室方法
        departmentService.save(map);
        return Result.ok();
    }

    // 查询医院
    @ApiOperation(value = "查询医院")
    @PostMapping("/hospital/show")
    public Result HospitalShow(HttpServletRequest request){
        Map<String, Object> map = HttpRequestHelper.switchMap(request.getParameterMap());
        // 签名验证
        apiUtil.signKey(map);
        Hospital hospital = hospitalService.getByHoscode((String)map.get("hoscode")); // 根据hoscode查询医院

        return Result.ok(hospital);
    }

    // 上传医院
    @ApiOperation(value = "上传医院")
    @PostMapping("/saveHospital")
    public Result saveHospital(HttpServletRequest request){
        // 以map格式获取请求参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> map = HttpRequestHelper.switchMap(parameterMap);

        // 签名验证
        apiUtil.signKey(map);

        // 解决图片转base64 +号变空格问题
        apiUtil.base64Image(map);

        hospitalService.save(map);
        return Result.ok();
    }
}
