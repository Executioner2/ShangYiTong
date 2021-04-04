package com.study.yygh.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 16:54
 * Versions:1.0.0
 * Description:
 */
public class IpUtil {
    /**
     * 获取请求用户的真实ip
     * @param request
     * @return
     */
    public static String getIpAddr(HttpServletRequest request){
        if (request.getHeader("x-forwarded-for") == null) {
            return request.getRemoteAddr();
        }
        return request.getHeader("x-forwarded-for");
    }
}
