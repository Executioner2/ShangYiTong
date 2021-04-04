package com.study.yygh.hosp.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.yygh.exception.YyghException;
import com.study.yygh.hosp.mapper.ScheduleMapper;
import com.study.yygh.hosp.repository.ScheduleRepository;
import com.study.yygh.hosp.service.DepartmentService;
import com.study.yygh.hosp.service.HospitalService;
import com.study.yygh.hosp.service.ScheduleService;
import com.study.yygh.model.hosp.BookingRule;
import com.study.yygh.model.hosp.Department;
import com.study.yygh.model.hosp.Hospital;
import com.study.yygh.model.hosp.Schedule;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.vo.hosp.BookingScheduleRuleVo;
import com.study.yygh.vo.hosp.ScheduleOrderVo;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-21 18:01
 * Versions:1.0.0
 * Description:
 */
@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, Schedule> implements ScheduleService {
    @Resource
    private ScheduleRepository scheduleRepository;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private HospitalService hospitalService;
    @Resource
    private DepartmentService departmentService;

    /**
     * 上传排班
     * @param map
     */
    @Override
    public void save(Map<String, Object> map) {
        Schedule schedule = JSONObject.parseObject(JSONObject.toJSONString(map), Schedule.class);
        // 根据 医院编号 和 排版编号查询排班
        Schedule scheduleExist = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(schedule.getHoscode(), schedule.getHosScheduleId());

        if(null != scheduleExist){
            schedule.setUpdateTime(new Date());
            schedule.setStatus(1);
            schedule.setIsDeleted(0);
            scheduleRepository.save(schedule);
        }else{
            schedule.setCreateTime(new Date());
            schedule.setUpdateTime(new Date());
            schedule.setIsDeleted(0);
            schedule.setStatus(1);
            scheduleRepository.save(schedule);
        }
    }

    /**
     * 分页显示排班
     * @param page
     * @param limit
     * @param hoscode
     */
    @Override
    public Page<Schedule> findPage(int page, int limit, String hoscode) {
        Pageable pageable = PageRequest.of(page - 1, limit);
        Schedule schedule = new Schedule();
        schedule.setHoscode(hoscode);
        ExampleMatcher exampleMatcher = ExampleMatcher.matching()
                .withIgnoreCase(true) // 忽略大小写
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING); // 改为模糊查询
        Example<Schedule> example = Example.of(schedule, exampleMatcher);
        Page<Schedule> all = scheduleRepository.findAll(example, pageable);
        return all;
    }

    /**
     * 根据查询出来的id删除排班
     * @param hoscode
     * @param hosScheduleId
     */
    @Override
    public void remove(String hoscode, String hosScheduleId) {
        Schedule schedule = scheduleRepository.getScheduleByHoscodeAndHosScheduleId(hoscode, hosScheduleId);
        if (schedule != null) {
            scheduleRepository.deleteById(schedule.getId());
        }
    }

    /**
     * 根据排班id获取排班数据
     * @param scheduleId
     * @return
     */
    @Override
    public Schedule getById(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        return this.packageSchedule(schedule);
    }

    /**
     * 根据医院编号 和 科室编号，查询排班规则数据
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Map<String, Object> getScheduleRule(Long page, Long limit, String hoscode, String depcode) {
        // 最终返回的结果
        Map<String, Object> result = new HashMap<>();
        // 使用 MongoTemplate 对mongoDB数据库进行聚合操作
        // 1、先设置排班的查询条件
        Criteria criteria = new Criteria("hoscode").is(hoscode).and("depcode").is(depcode);

        // 2、根据工作日workDate进行分组
        // 这里使用Aggregation（对mongoDB进行聚合操作的类，没有public构造函数，适用于MongoTemplate）
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria), // 匹配条件
                Aggregation.group("workDate") // 分组字段
                        .first("workDate").as("workDate") // 给查询结果中的分组字段取个别名
                        // 根据docCount字段统计当天上班的医生数量
                        .count().as("docCount")
                        .sum("reservedNumber").as("reservedNumber") // 对上班的每个医生的预约数进行累加，并给结果取个别名.as("reservedNumber")
                        .sum("availableNumber").as("availableNumber"), // 对剩余预约数进行累加并取别名
                // 3、按workDate进行降序排序
                Aggregation.sort(Sort.Direction.DESC, "workDate"),
                // 4、进行分页
                Aggregation.skip((page - 1) * limit),
                Aggregation.limit(limit)
        );

        // 5、最终执行查询
        // 参数1：aggregate聚合条件
        // 参数2：mongoDB中的数据模型
        // 参数3：要返回的数据类型
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = mongoTemplate.aggregate(agg, Schedule.class, BookingScheduleRuleVo.class).getMappedResults();

        // 分组查询的总记录数
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group("workDate")
        );
        AggregationResults<BookingScheduleRuleVo> totalResult = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        int total = totalResult.getMappedResults().size();

        // 显示星期
        for (BookingScheduleRuleVo bsrv : bookingScheduleRuleVoList) {
            String dayOfWeek = this.getDayOfWeek(new DateTime(bsrv.getWorkDate()));
            bsrv.setDayOfWeek(dayOfWeek);
        }

        // 整理最终结果进行返回
        result.put("bookingScheduleRuleList", bookingScheduleRuleVoList);
        result.put("total", total);

        // 把医院名称前再套入一个map，方便前端显示
        // TODO 暂时没用上，给注释掉
        // 获取医院名称
        /*String hospName = hospitalService.getHospName(hoscode);
        Map<String, String> baseMap = new HashMap<>();
        baseMap.put("hospName", hospName);
        result.put("baseMap", baseMap);*/

        return result;
    }

    /**
     * 根据 hoscode 和 workDate 查询排班详情列表
     * @param hoscode
     * @param workDate
     * @return
     */
    @Override
    public List<Schedule> getScheduleDetails(String hoscode, String depcode, String workDate) {
        List<Schedule> list = scheduleRepository.findScheduleByHoscodeAndDepcodeAndWorkDate(hoscode, depcode, new DateTime(workDate).toDate());
        return list;
    }

    /**
     * 获取可预约排班信息
     * @param page
     * @param limit
     * @param hoscode
     * @param depcode
     * @return
     */
    @Override
    public Map<String, Object> getBookingScheduleRule(Integer page, Integer limit, String hoscode, String depcode) {
        Map<String, Object> result = new HashMap<>();
        // 先根据hoscode查询出医院
        Hospital hospital = hospitalService.getByHoscode(hoscode);
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        // 得到医院的预约规则
        BookingRule bookingRule = hospital.getBookingRule();
        // 获取可预约日期分页数据
        IPage iPage = this.getListDate(page, limit, bookingRule);
        // 当前页的可预约日期
        List<Date> dateList = iPage.getRecords();
        // 查询mongoDB中的可预约数
        Criteria criteria = Criteria.where("hoscode").is(hoscode).and("depcode").is(depcode).and("workDate").in(dateList);
        // 对可预约数做一个聚合操作
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria), // 查询条件
                Aggregation.group("workDate")
                        .first("workDate").as("workDate") // 分组字段
                        .count().as("docCount") // 计数一共多少条数据
                        .sum("availableNumber").as("availableNumber") // 可预约数总和
                        .sum("reservedNumber").as("reservedNumber") // 剩余预约数总和
        );
        AggregationResults<BookingScheduleRuleVo> aggregate = mongoTemplate.aggregate(aggregation, Schedule.class, BookingScheduleRuleVo.class);
        List<BookingScheduleRuleVo> mappedResults = aggregate.getMappedResults(); // 获取排班信息
        // 把排班信息转换成一个map集合，方便前端操作
        // 将统计数据ScheduleVo根据“安排日期”合并到BookingRuleVo
        // bookingScheduleRuleVo.getWorkDate() 为key
        // bookingScheduleRuleVo 自身为value
        Map<Date, BookingScheduleRuleVo> scheduleVoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(mappedResults)) {  // 如果排班信息不为空才开始操作
            scheduleVoMap = mappedResults.stream().collect(Collectors.toMap(
                    BookingScheduleRuleVo::getWorkDate,
                    BookingScheduleRuleVo -> BookingScheduleRuleVo));
        }

        // 搞预约规则
        List<BookingScheduleRuleVo> bookingScheduleRuleVoList = new ArrayList<>();
        for (int i = 0, len = dateList.size(); i < len; i++) {
            Date date = dateList.get(i);
            BookingScheduleRuleVo bookingScheduleRuleVo = scheduleVoMap.get(date);
            if (bookingScheduleRuleVo == null) { // 如果 bookingScheduleRuleVo 为null说明当天没有排班
                // 为null还是要new一个出来，把参数设置为代表没有排班的值，这样便于前端显示
                bookingScheduleRuleVo = new BookingScheduleRuleVo();
                // 设置就诊医生数为 0
                bookingScheduleRuleVo.setDocCount(0);
                // 设置科室剩余预约数为 -1
                bookingScheduleRuleVo.setAvailableNumber(-1);
            }
            bookingScheduleRuleVo.setWorkDate(date); // 设置工作时间
            bookingScheduleRuleVo.setWorkDateMd(date); // 同上，别问为什么要搞一个相同的，问就是方便前端页面展示
            // 计算当天为周几
            String dayOfWeek = this.getDayOfWeek(new DateTime(date));
            bookingScheduleRuleVo.setDayOfWeek(dayOfWeek);
            // 最后一页的最后一条记录为即将预约 状态 0：正常  1：即将放号  -1：当天已停止挂号
            if(i == len - 1 && page == iPage.getPages()){
                bookingScheduleRuleVo.setStatus(1);
            }else{
                bookingScheduleRuleVo.setStatus(0);
            }
            // 当天预约如果超过了预约时间则不能预约
            if(i == 0 && page == 1){ // 页面第一页第一条记录是当天
                // 得到今天停止预约的时间
                DateTime dateTime = this.getDateTime(new Date(), bookingRule.getStopTime());
                if(dateTime.isBeforeNow()){ // 今天停止预约的时间已经过去了
                    bookingScheduleRuleVo.setStatus(-1);
                }
            }
            bookingScheduleRuleVoList.add(bookingScheduleRuleVo);
        }

        // 把值放到做返回结果的map中
        // 可预约日期规则数据
        result.put("bookingScheduleList", bookingScheduleRuleVoList);
        result.put("total", iPage.getTotal());
        // 其他基础数据
        Map<String, String> baseMap = new HashMap<>();
        // 医院名称
        baseMap.put("hosname", hospitalService.getHospName(hoscode));
        // 科室
        Department department = departmentService.getDepartment(hoscode, depcode);
        // 大科室名称
        baseMap.put("bigname", department.getBigname());
        // 科室名称
        baseMap.put("depname", department.getDepname());
        // 月
        baseMap.put("workDateString", new DateTime().toString("yyyy年MM月"));
        // 放号时间
        baseMap.put("releaseTime", bookingRule.getReleaseTime());
        // 停号时间
        baseMap.put("stopTime", bookingRule.getStopTime());
        result.put("baseMap", baseMap);
        return result;

    }

    /**
     * 根据排班id获取预约下单数据
     * @param scheduleId
     * @return
     */
    @Override
    public ScheduleOrderVo getScheduleOrderVo(String scheduleId) {
        ScheduleOrderVo scheduleOrderVo = new ScheduleOrderVo();
        // 获取排班信息
        Schedule schedule = this.getScheduleId(scheduleId);
        if (schedule == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 根据hoscode查出医院
        Hospital hospital = hospitalService.getByHoscode(schedule.getHoscode());
        if (hospital == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        // 获得该医院的排版规则
        BookingRule bookingRule = hospital.getBookingRule();
        if (bookingRule == null) {
            throw new YyghException(ResultCodeEnum.DATA_ERROR);
        }
        Integer cycle = bookingRule.getCycle(); // 预约周期
        String depName = departmentService.getDepName(schedule.getHoscode(), schedule.getDepcode()); // 科室名称

        scheduleOrderVo.setHosname(hospital.getHosname()); // 医院名称
        scheduleOrderVo.setHoscode(hospital.getHoscode()); // 医院编号
        scheduleOrderVo.setHosScheduleId(schedule.getHosScheduleId()); // 排班编号
        scheduleOrderVo.setDepcode(schedule.getDepcode()); // 科室编号
        scheduleOrderVo.setTitle(schedule.getTitle()); // 医生职称
        scheduleOrderVo.setDepname(depName); // 科室名称
        scheduleOrderVo.setAmount(schedule.getAmount()); // 费用
        scheduleOrderVo.setAvailableNumber(schedule.getAvailableNumber()); // 剩余预约数
        scheduleOrderVo.setReserveTime(schedule.getWorkTime()); // 安排时间
        scheduleOrderVo.setReserveDate(schedule.getWorkDate()); // 安排日期

        // 设置退号日期
        Integer quitDay = bookingRule.getQuitDay(); // 退号日期，如果为前一天则是 -1，如果是当天则是0
        DateTime quitTime = this.getDateTime(new DateTime(schedule.getWorkDate()).plusDays(quitDay).toDate(), bookingRule.getQuitTime());
        scheduleOrderVo.setQuitTime(quitTime.toDate());

        // 设置挂号开始时间
        DateTime startTime = this.getDateTime(new Date(), bookingRule.getReleaseTime());
        scheduleOrderVo.setStartTime(startTime.toDate());

        // 设置挂号结束时间
        DateTime endTime = this.getDateTime(new DateTime().plusDays(cycle).toDate(), bookingRule.getStopTime());
        scheduleOrderVo.setEndTime(endTime.toDate());

        // 设置挂号停止时间
        DateTime stopTime = this.getDateTime(new Date(), bookingRule.getStopTime());
        scheduleOrderVo.setStopTime(stopTime.toDate());

        return scheduleOrderVo;
    }

    /**
     * 获取排班信息
     * @param scheduleId
     * @return
     */
    @Override
    public Schedule getScheduleId(String scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId).get();
        return this.packageSchedule(schedule);
    }

    /**
     * 更新数据库
     * @param schedule
     */
    @Override
    public void update(Schedule schedule) {
        schedule.setUpdateTime(new Date());
        scheduleRepository.save(schedule);
    }


    /**
     * 获取可预约日期分页数据
     * @param page
     * @param limit
     * @param bookingRule
     * @return
     */
    private IPage<Date> getListDate(Integer page, Integer limit, BookingRule bookingRule) {
        Integer cycle = bookingRule.getCycle(); // 周期，每个医院有几天预约
        DateTime releaseTime = this.getDateTime(new Date(), bookingRule.getReleaseTime()); // 今日的预约时间
        // 如果今天的时间已经过了今天的预约时间，则预约周期加1
        if(releaseTime.isAfterNow()) cycle++;
        // 算出预约周期内每一天的
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            DateTime curDateTime = new DateTime().plusDays(i);
            String s = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(s).toDate());
        }
        // 日期分页显示
        List<Date> pageDateList = new ArrayList<>();
        int start = (page - 1) * limit;
        int end = start + limit;
        if(cycle < end) end = cycle;
        for(int i = start; i < end; i++){
            pageDateList.add(dateList.get(i));
        }
        // 参数依次为：起始页、每页数据数、总数据数（给个总数据数是为了前端展示）
        IPage<Date> iPage = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, 7, cycle);
        iPage.setRecords(pageDateList); // 把分页后的数据放到iPage中
        return iPage;
    }

    /**
     * 将Date日期（yyyy-MM-dd HH:mm）转换为DateTime
     */
    private DateTime getDateTime(Date date, String timeString) {
        String dateTimeString = new DateTime(date).toString("yyyy-MM-dd") + " "+ timeString;
        DateTime dateTime = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").parseDateTime(dateTimeString);
        return dateTime;
    }

    /**
     * 封装排班详情其他值 医院名称、科室名称、日期对应星期
     * @param schedule
     * @return
     */
    private Schedule packageSchedule(Schedule schedule) {
        // 设置医院名称
        schedule.getParam().put("hosname",hospitalService.getHospName(schedule.getHoscode()));
        // 设置科室名称
        schedule.getParam().put("depname",departmentService.getDepName(schedule.getHoscode(),schedule.getDepcode()));
        // 设置日期对应星期
        schedule.getParam().put("dayOfWeek",this.getDayOfWeek(new DateTime(schedule.getWorkDate())));
        return schedule;
    }


    /**
     * 根据DateTime返回星期X
     * @param dateTime
     * @return
     */
    private String getDayOfWeek(DateTime dateTime) {
        String dayOfWeek = null;
        switch (dateTime.getDayOfWeek()){
            case DateTimeConstants.SUNDAY:
                dayOfWeek = "周日";
                break;
            case DateTimeConstants.MONDAY:
                dayOfWeek = "周一";
                break;
            case DateTimeConstants.TUESDAY:
                dayOfWeek = "周二";
                break;
            case DateTimeConstants.WEDNESDAY:
                dayOfWeek = "周三";
                break;
            case DateTimeConstants.THURSDAY:
                dayOfWeek = "周四";
                break;
            case DateTimeConstants.FRIDAY:
                dayOfWeek = "周五";
                break;
            case DateTimeConstants.SATURDAY:
                dayOfWeek = "周六";
            default:
                break;
        }

        return dayOfWeek;
    }

}
