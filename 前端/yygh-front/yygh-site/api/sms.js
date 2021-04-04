import request from '@/utils/request'

const api_name = '/api/msm'

export default{
    // 短信服务
    sendCode(phone){
        return request({
            url: `${api_name}/send/${phone}`,
            method: 'get'
        })
    }
}