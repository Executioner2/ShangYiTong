package com.study.yygh.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.study.yygh.model.user.UserInfo;
import com.study.yygh.result.Result;
import com.study.yygh.user.service.UserInfoService;
import com.study.yygh.vo.user.UserInfoQueryVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 16:04
 * Versions:1.0.0
 * Description:
 */

@Api(tags = "后台用户信息管理")
@RestController
@RequestMapping("/admin/user")
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    // 分页条件显示用户列表
    @ApiOperation(value = "分页条件显示用户列表")
    @GetMapping("/{page}/{limit}")
    public Result list(@PathVariable Long page,
                       @PathVariable Long limit,
                       UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> userInfoPage = new Page<>(page, limit);
        IPage<UserInfo> pageModel = userInfoService.selectPage(userInfoPage, userInfoQueryVo);
        return Result.ok(pageModel);
    }

    // 用户锁定
    @ApiOperation(value = "用户锁定")
    @GetMapping("/lock/{userId}/{status}")
    public Result lock(@PathVariable Long userId,
                       @PathVariable Integer status){
        userInfoService.lock(userId, status);
        return Result.ok();
    }

    // 显示用户详情
    @ApiOperation(value = "显示用户详情")
    @GetMapping("/show/{userId}")
    public Result show(@PathVariable Long userId){
        Map<String, Object> map = userInfoService.show(userId);
        return Result.ok(map);
    }

    // 用户认证审批
    @ApiOperation(value = "用户认证审批")
    @GetMapping("/approval/{userId}/{authStatus}")
    public Result approval(@PathVariable Long userId,
                           @PathVariable Integer authStatus){
        userInfoService.approval(userId, authStatus);
        return Result.ok();

    }


}

