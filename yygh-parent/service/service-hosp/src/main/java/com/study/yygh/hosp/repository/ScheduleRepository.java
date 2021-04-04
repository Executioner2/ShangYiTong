package com.study.yygh.hosp.repository;

import com.study.yygh.model.hosp.Schedule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:00
 * Versions:1.0.0
 * Description:
 */
@Repository
public interface ScheduleRepository extends MongoRepository<Schedule, String> {
    // 根据 hoscode 和 hosScheduleId 查询排班
    Schedule getScheduleByHoscodeAndHosScheduleId(String hoscode, String hosScheduleId);

    // 根据 hoscode depcode 和 workDate 查询排班详情列表
    List<Schedule> findScheduleByHoscodeAndDepcodeAndWorkDate(String hoscode, String depcode, Date workDate);
}
