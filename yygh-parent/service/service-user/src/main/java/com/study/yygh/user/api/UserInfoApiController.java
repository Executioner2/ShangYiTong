package com.study.yygh.user.api;

import com.study.yygh.model.user.UserInfo;
import com.study.yygh.result.Result;
import com.study.yygh.user.service.UserInfoService;
import com.study.yygh.util.AutoContextHolder;
import com.study.yygh.vo.user.LoginVo;
import com.study.yygh.vo.user.UserAuthVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 21:54
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "前台用户管理")
@RequestMapping("/api/user")
@RestController
public class UserInfoApiController {
    @Resource
    private UserInfoService userInfoService;

    // 用户登录
    @ApiOperation(value = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginVo loginVo, HttpServletRequest request){
        //loginVo.setIp(IpUtil.getIpAddr(request));
        Map<String, Object> map = userInfoService.login(loginVo);
        return Result.ok(map);
    }

    // 用户认证接口
    @ApiOperation(value = "前台用户认证")
    @PutMapping("/auth/userAuth")
    public Result userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request){
        // 通过AutoContextHolder工具类从请求头中得到userID
        Long userId = AutoContextHolder.getUserId(request);
        // 添加用户认证
        userInfoService.userAuth(userId, userAuthVo);
        return Result.ok();
    }

    // 根据用户id获取用户详情信息
    @ApiOperation(value = "根据用户id获取用户详情信息")
    @GetMapping("/auth/getUserInfo")
    public Result getUserInfo(HttpServletRequest request){
        UserInfo userInfo = userInfoService.getById(AutoContextHolder.getUserId(request));
        return Result.ok(userInfo);
    }
}
