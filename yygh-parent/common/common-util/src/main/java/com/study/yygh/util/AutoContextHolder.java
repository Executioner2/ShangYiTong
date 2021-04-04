package com.study.yygh.util;

import javax.servlet.http.HttpServletRequest;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 22:01
 * Versions:1.0.0
 * Description: 因为前端已经把token写入到了请求头中，所以可以通过token获得用户信息
 *              根据传来的request，获取请求头中token，再得到用户信息
 */
public class AutoContextHolder {
    /**
     * 根据request中的token获取用户名
     * @param request
     * @return
     */
    public static String getUserName(HttpServletRequest request){
        // 获取请求头中的token
        String token = request.getHeader("token");
        // 通过JwtHelper工具类获取token中的用户名
        String userName = JwtHelper.getUserName(token);
        return userName;
    }

    /**
     * 根据request中的token获取用户ID
     * @param request
     * @return
     */
    public static Long getUserId(HttpServletRequest request){
        // 获取请求头中的token
        String token = request.getHeader("token");
        // 通过JwtHelper工具类获取token中的用户ID
        Long userId = JwtHelper.getUserId(token);
        return userId;
    }
}
