import request from '@/utils/request'

const api_name = '/api/ucenter/wx'

export default {
    // 获取生成二维码的参数
    getLoginParam(){
        return request({
            url: `${api_name}/getLoginParam`,
            method: 'get'
        })
    },
    // 微信登录轮询确认
    polling(uuid){
        return request({
            url: `${api_name}/getToken/${uuid}`,
            method: 'get'
        })
    },
    // 生成支付二维码
    createNative(orderId) {
        return request({
            url: `/api/order/weixin/createNative/${orderId}`,
            method: 'get'
        })
    },
    // 轮询查询是否支付成功
    queryPayStatus(orderId) {
        return request({
            url: `/api/order/weixin/queryPayStatus/${orderId}`,
            method: 'get'
        })
    }   
}