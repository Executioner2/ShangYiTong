import request from '@/utils/request'

const api_name = '/api/hosp/hospital'

export default{
    // 按条件分页显示医院
    getPageList(page, limit, searchObj){
        return request({
            url: `${api_name}/show/${page}/${limit}`,
            method: 'get',
            params: searchObj
        })
    },
    // 根据不完全的医院名称联想出医院列表
    getHospByHosnameLike(hosname){
        return request({
            url: `${api_name}/findByHosname/${hosname}`,
            method: 'get'
        })
    },
    // 获取医院详情
    show(hoscode){
        return request({
            url: `${api_name}/${hoscode}`,
            method: 'get'
        })
    },
    // 获取排班列表
    findDepartmentByHoscode(hoscode){
        return request({
            url: `${api_name}/department/${hoscode}`,
            method: 'get'
        })
    },
    // 显示预约日期
    getBookingScheduleRule(page, limit, hoscode, depcode) {
        return request({
            url: `${api_name}/auth/getBookingScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
            method: 'get'
        })
    },
    // 显示科室排班信息
    findScheduleList(hoscode, depcode, workDate) {
        return request({
            url: `${api_name}/auth/findScheduleList/${hoscode}/${depcode}/${workDate}`,
            method: 'get'
        })
    },
    // 显示科室信息
    getSchedule(id) {
        return request({
          url: `${api_name}/getSchedule/${id}`,
          method: 'get'
        })
    }    
}