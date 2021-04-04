import request from '@/utils/request'

const api_name = '/api/order/orderInfo'

export default{
    // 提交订单
    submitOrder(scheduleId, patientId) {
        return request({
          url: `${api_name}/auth/submitOrder/${scheduleId}/${patientId}`,
          method: 'post'
        })
    },
    //订单列表
    getPageList(page, limit, searchObj) {
        return request({
            url: `${api_name}/auth/${page}/${limit}`,
            method: `get`,
            params: searchObj
        })
    },
    //订单状态
    getStatusList() {
        return request({
            url: `${api_name}/auth/getStatusList`,
            method: 'get'
        })
    },
    //订单详情
    getOrderInfo(orderId){
        return request({
            url: `${api_name}/auth/get/${orderId}`,
            method: 'get'
        })
    },
    // 微信退款    
    cancelOrder(orderId) {
        return request({
            url: `${api_name}/auth/cancelOrder/${orderId}`,
            method: 'get'
        })
    }, 
}