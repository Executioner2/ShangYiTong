import request from '@/utils/request'


export default{
    // 医院列表显示
    getPageList(page, size, searchObj){
        return request({
            url: `/admin/hosp/hospital/list/${page}/${size}`,
            method: 'get',
            params: searchObj
        })
    },
    // 根据dictCode做子查询
    findByDictCode(dictCode){
        return request({
            url: `/admin/cmn/dict/findByDictCode/${dictCode}`,
            method: 'get'
        })
    },
    // 根据id做子查询
    findByDictId(id){
        return request({
            url: `/admin/cmn/dict/findChildData/${id}`,
            method: 'get'
        })
    },
    // 上线和下线
    updateStatus(id, status){
        return request({
            url: `/admin/hosp/hospital/update/${id}/${status}`,
            method: 'put'
        })
    },
    // 显示医院详情
    show(id){
        return request({
            url: `/admin/hosp/hospital/show/${id}`,
            method: 'get'
        })
    },
    // 显示排班信息
    getDeptByHoscode(hoscode){
        return  request({
            url: `/admin/hosp/department/getDeptList/${hoscode}`,
            method: 'get'
        })
    },
    // 查询排班规则数据
    getScheduleRule(page, limit, hoscode, depcode){
        return request({
            url:`/admin/hosp/department/getScheduleRule/${page}/${limit}/${hoscode}/${depcode}`,
            method: "get"
        })
    },
    // 查询排班详情列表
    getScheduleDetails(hoscode, depcode, workDate){
        return request({
            url:`/admin/hosp/department/getScheduleDetails/${hoscode}/${depcode}/${workDate}`,
            method: "get"
        })
    }

    
}