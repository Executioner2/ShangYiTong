package com.study.yygh.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.user.UserInfo;
import com.study.yygh.vo.user.LoginVo;
import com.study.yygh.vo.user.UserAuthVo;
import com.study.yygh.vo.user.UserInfoQueryVo;

import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 13:37
 * Versions:1.0.0
 * Description:
 */
public interface UserInfoService extends IService<UserInfo> {
    // 用户登录
    Map<String, Object> login(LoginVo loginVo);

    // 根据微信openid获取用户信息
    UserInfo getByOpenid(String openid);

    // 用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);

    // 分页条件显示用户列表
    IPage<UserInfo> selectPage(Page<UserInfo> userInfoPage, UserInfoQueryVo userInfoQueryVo);

    // 用户锁定
    void lock(Long userId, Integer status);

    // 显示用户详情
    Map<String, Object> show(Long userId);

    // 用户认证审批
    void approval(Long userId, Integer authStatus);
}
