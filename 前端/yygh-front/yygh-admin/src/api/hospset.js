import request from '@/utils/request'

const api_name = '/admin/hosp/hospitalSet'

export default {
    // 分页查询hospitalSet
    getPageList(current, limit, searchObj) {
        return request({
            url: `${api_name}/findPageHospSet/${current}/${limit}`,
            method: 'post',
            data: searchObj
        })
    },
    // 根据id进行删除
    removeHospSetById(id){
        return request({
            url: `${api_name}/${id}`,
            method: 'delete'            
        })
    },
    // 批量删除
    batchRemoveHospSet(idList){
        return request({
            url: `${api_name}/batchRemove`,
            method: 'delete',
            data: idList 
        })
    },
    // 锁定和取消锁定
    lockHospSet(id, status){
        return request({
            url: `${api_name}/lockHospitalSet/${id}/${status}`,
            method: 'put'
        })
    },
    // 添加医院设置
    addHospSet(hospitalSet){
        return request({
            url: `${api_name}/saveHospitalSet`,
            method: 'post',
            data: hospitalSet
        })
    },
    // 根据id查询医院
    getHospSetById(id){
        return request({
            url: `${api_name}/getHospSet/${id}`,
            method: 'get'
        })
    },
    // 医院更新
    updateHospSet(hospitalSet){
        return request({
            url: `${api_name}/updateHospSet`,
            method: 'put',
            data: hospitalSet
        })
    }
}
  