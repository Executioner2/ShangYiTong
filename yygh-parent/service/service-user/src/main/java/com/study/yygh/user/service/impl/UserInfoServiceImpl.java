package com.study.yygh.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.yygh.enums.AuthStatusEnum;
import com.study.yygh.exception.YyghException;
import com.study.yygh.model.user.Patient;
import com.study.yygh.model.user.UserInfo;
import com.study.yygh.result.ResultCodeEnum;
import com.study.yygh.user.mapper.UserInfoMapper;
import com.study.yygh.user.service.PatientService;
import com.study.yygh.user.service.UserInfoService;
import com.study.yygh.util.JwtHelper;
import com.study.yygh.vo.user.LoginVo;
import com.study.yygh.vo.user.UserAuthVo;
import com.study.yygh.vo.user.UserInfoQueryVo;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-27 13:49
 * Versions:1.0.0
 * Description:
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private PatientService patientService;

    /**
     * 用户登录
     * @param loginVo
     * @return
     */
    @Override
    public Map<String, Object> login(LoginVo loginVo) {
        String phone = loginVo.getPhone();
        String code = loginVo.getCode();

        // 判断手机号是否为空
        if(StringUtils.isEmpty(phone)){ // 如果未空则抛出参数不正确错误
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 从redis中查询sms发送的验证码
        String smsCode = redisTemplate.opsForValue().get(phone);
        if(!code.equals(smsCode)){ // 判断登录验证码和redis中的是否一致
            // 如果不相等则返回验证码错误异常
            throw new YyghException(ResultCodeEnum.CODE_ERROR);
        }

        // 绑定手机号码
        // TODO 实现微信登录后加上去的
        UserInfo userInfo = null;
        if(!StringUtils.isEmpty(loginVo.getOpenid())) {
            userInfo = this.getByOpenid(loginVo.getOpenid());
            if(null != userInfo) {
                userInfo.setPhone(loginVo.getPhone());
                this.updateById(userInfo);
            } else {
                throw new YyghException(ResultCodeEnum.DATA_ERROR);
            }
        }

        if(null == userInfo) {
            // 根据手机号查询数据库取得用户
            QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
            wrapper.eq("phone", phone);
            userInfo = baseMapper.selectOne(wrapper);

            // 如果没有用户则创建用户
            if (null == userInfo) {
                userInfo = new UserInfo();
                userInfo.setName(null);
                userInfo.setPhone(phone);
                userInfo.setCreateTime(new Date());
                userInfo.setStatus(1); // 未锁定
                baseMapper.insert(userInfo);
            } else {
                // 查看用户的lockStatus
                if (userInfo.getStatus() == 0) {
                    throw new YyghException(ResultCodeEnum.LOGIN_DISABLED_ERROR);
                }
            }
        }

        // 搞出需要返回的结果参数
        String name = userInfo.getName();
        if(StringUtils.isEmpty(name)){ // 如果用户名为空则取得用户昵称
            name = userInfo.getNickName();
        }
        if(StringUtils.isEmpty(name)){ // 如果用户昵称也是空的则用手机号做name
            name = userInfo.getPhone();
        }
        // token  进行base64转码（不是加密，可以对token做加密处理）处理后的用户登录信息，放入cookie中
        String token = JwtHelper.createToken(userInfo.getId(), name);

        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("token", token);

        return map;
    }

    /**
     * 根据微信openid获取用户信息
     * @param openid
     * @return
     */
    @Override
    public UserInfo getByOpenid(String openid) {
        return baseMapper.selectOne(new QueryWrapper<UserInfo>().eq("openid", openid));
    }


    /**
     * 用户认证
     * @param userId
     * @param userAuthVo
     */
    @Override
    public void userAuth(Long userId, UserAuthVo userAuthVo) {
        // 根据用户id查询用户信息
        UserInfo userInfo = baseMapper.selectById(userId);
        userInfo.setName(userAuthVo.getName()); // 设置认证人姓名
        userInfo.setCertificatesNo(userAuthVo.getCertificatesNo()); // 设置证件号码
        userInfo.setCertificatesType(userAuthVo.getCertificatesType()); // 设置证件类型
        userInfo.setCertificatesUrl(userAuthVo.getCertificatesUrl()); // 设置在oss中头像的url
        userInfo.setAuthStatus(AuthStatusEnum.AUTH_RUN.getStatus()); // 设置认证状态为正在认证中
        // 更新数据库中的数据
        baseMapper.updateById(userInfo);
    }

    /**
     * 分页条件显示用户列表
     * @param userInfoPage
     * @param userInfoQueryVo
     * @return
     */
    @Override
    public IPage<UserInfo> selectPage(Page<UserInfo> userInfoPage, UserInfoQueryVo userInfoQueryVo) {
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();

        Integer status = userInfoQueryVo.getStatus(); // 用户锁定状态
        Integer authStatus = userInfoQueryVo.getAuthStatus(); // 用户认证状态（未认证，认证中，认证通过，认证未通过）
        String createTimeBegin = userInfoQueryVo.getCreateTimeBegin(); // 创建时间始
        String createTimeEnd = userInfoQueryVo.getCreateTimeEnd();  // 创建时间末
        String keyword = userInfoQueryVo.getKeyword(); // 用户名或手机号称关键字

        // 判断用户名称或手机号关键字是否为空
        if(!StringUtils.isEmpty(keyword)){ // 如果不为空模糊查询匹配name
            // 这种写法相当于    有括号
            // select * from user where status = '1' and (name like '%张%' or phone like '%张%')
            wrapper.and(qw->qw.like("name", keyword).or().like("phone", keyword));
            // 而下面这个相当于   无括号
            // select * from user where status = '1' and name like '%张%' or phone like '%张%'
            //wrapper.like("name", keyword).or().like("phone", keyword);
        }
        // 用户状态，前端会传来固定的1，1为未锁定
        if(!StringUtils.isEmpty(status)){
            wrapper.eq("status", status);
        }
        // 用户认证状态
        if(!StringUtils.isEmpty(authStatus)){
            wrapper.eq("auth_status", authStatus);
        }
        // 创建时间区间
        if(!StringUtils.isEmpty(createTimeBegin)){ // 创建时间大于createTimeBegin
            wrapper.ge("create_time", createTimeBegin);
        }
        if(!StringUtils.isEmpty(createTimeEnd)){ // 创建时间小于createTimeEnd
            wrapper.le("create_time", createTimeEnd);
        }
        IPage<UserInfo> pages = baseMapper.selectPage(userInfoPage, wrapper);
        // 把userInfo编号变成对应值的封装
        pages.getRecords().stream().forEach(item -> {
            this.userInfoPackage(item);
        });
        return pages;
    }

    /**
     * 用户锁定
     * @param userId
     * @param status
     */
    @Override
    public void lock(Long userId, Integer status) {
        if(status.intValue() == 1 || status.intValue() == 0){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setStatus(status);
            this.updateById(userInfo);
        }
    }

    /**
     * 显示用户详情
     * @param userId
     * @return
     */
    @Override
    public Map<String, Object> show(Long userId) {
        Map<String, Object> map = new HashMap<>();
        // 根据Id查询userInfo
        UserInfo userInfo = baseMapper.selectById(userId);
        // 根据Id就诊人信息
        List<Patient> patientList = patientService.findAll(userId);
        map.put("userInfo", userInfo);
        map.put("patientList", patientList);
        return map;
    }

    /**
     * 用户认证审批
     * @param userId
     * @param authStatus
     */
    @Override
    public void approval(Long userId, Integer authStatus) {
        if(authStatus.intValue() == 2 || authStatus.intValue() == -1){
            UserInfo userInfo = new UserInfo();
            userInfo.setId(userId);
            userInfo.setAuthStatus(authStatus);
            this.updateById(userInfo);
        }
    }

    /**
     * 把userInfo编号变成对应值的封装
     * @param userInfo
     */
    private UserInfo userInfoPackage(UserInfo userInfo) {
        // 处理认证状态编码
        userInfo.getParam().put("authStatusString", AuthStatusEnum.getStatusNameByStatus(userInfo.getAuthStatus()));
        // 处理用户状态 0 or 1
        String statusString = userInfo.getStatus().intValue() == 0 ? "锁定" : "认证";
        userInfo.getParam().put("statusString", statusString);
        return userInfo;
    }
}
