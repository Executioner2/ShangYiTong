import request from '@/utils/request'

const api_name = '/admin/cmn/dict'

export default{
    // 获取医院等级和地区
    findByDictCode(dictCode){
        return request({
            url: `${api_name}/findByDictCode/${dictCode}`,
            method: 'get'
        })
    },
    // 根据id查询所有子节点
    findChildData(id){
        return request({
            url: `${api_name}/findChildData/${id}`,
            method: 'get'
        })
    }    
}