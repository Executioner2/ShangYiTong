package com.study.yygh.hosp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.vo.hosp.ScheduleOrderVo;
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
public interface ScheduleService extends IService<Schedule> {
    // 上传排班
    void save(Map<String, Object> map);

    // 分页显示排班
    Page<Schedule> findPage(int page, int limit, String hoscode);

    // 删除排班
    void remove(String hoscode, String hosScheduleId);

    // 根据排班id获取排班数据
    Schedule getById(String scheduleId);

    // 根据医院编号 和 科室编号，查询排班规则数据
    Map<String, Object> getScheduleRule(Long page, Long limit, String hoscode, String depcode);

    // 根据 hoscode 和 workDate 查询排班详情列表
    List<Schedule> getScheduleDetails(String hoscode, String depcode, String workDate);

    // 获取可预约排班信息
    Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode);

    // 根据排班id获取预约下单数据
    ScheduleOrderVo getScheduleOrderVo(String scheduleId);

    // 获取排班信息
    Schedule getScheduleId(String scheduleId);

    // 更新数据库
    void update(Schedule schedule);
}
