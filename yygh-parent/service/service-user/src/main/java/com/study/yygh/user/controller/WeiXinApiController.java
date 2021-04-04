package com.study.yygh.user.controller;

import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.study.yygh.model.user.UserInfo;
import com.study.yygh.result.Result;
import com.study.yygh.user.service.UserInfoService;
import com.study.yygh.user.util.ConstantWxPropertiesUtil;
import com.study.yygh.user.util.HttpClientUtils;
import com.study.yygh.util.JwtHelper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-28 14:53
 * Versions:1.0.0
 * Description:
 */
@Controller
@Api(tags = "微信登陆管理")
@RequestMapping("/api/ucenter/wx")
public class WeiXinApiController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private RedisTemplate redisTemplate;

    //这里的token要和微信测试号网页填写的token一样
    public static final String TOKEN = "okjfdlsf_lsdfjdslkfj_token";

    @GetMapping("/get")
    public void get(String signature,String timestamp,String nonce,String echostr, HttpServletResponse response) throws IOException, NoSuchAlgorithmException, IOException {
        // 将token、timestamp、nonce三个参数进行字典序排序
        System.out.println("signature:"+signature);
        System.out.println("timestamp:"+timestamp);
        System.out.println("nonce:"+nonce);
        System.out.println("echostr:"+echostr);
        System.out.println("TOKEN:"+TOKEN);
        String[] params = new String[] { TOKEN, timestamp, nonce };
        Arrays.sort(params);
        // 将三个参数字符串拼接成一个字符串进行sha1加密
        String clearText = params[0] + params[1] + params[2];
        String algorithm = "SHA-1";
        String sign = new String(
                org.apache.commons.codec.binary.Hex.encodeHex(MessageDigest.getInstance(algorithm).digest((clearText).getBytes()), true));
        // 开发者获得加密后的字符串可与signature对比，标识该请求来源于微信
        if (signature.equals(sign)) {
            response.getWriter().print(echostr);
        }
    }

    // 获取微信登录参数，返回生成二维码所需要的参数
    @ApiOperation(value = "获取微信登录参数")
    @GetMapping("/getLoginParam")
    @ResponseBody
    public Result getLoginParam() {
        try {
            Map<String, String> map = new HashMap<>();
            // 把请求地址转成utf-8编码
            String encode = URLEncoder.encode(ConstantWxPropertiesUtil.WX_OPEN_REDIRECT_URL, "utf-8");
            // 生成一个uuid
            String uuid = IdUtil.randomUUID().replaceAll("-", "");
            // 返回一个字符串
            /**
             * https://open.weixin.qq.com/connect/oauth2/authorize
             * ?appid=wxff5db53def71d004
             * &redirect_uri=http%3A%2F%2Fddwhie.natappfree.cc%2Fapi%2Fucenter%2Fwx%2Fcallback
             * &response_type=code
             * &scope=snsapi_userinfo
             * &state=1616943175712#wechat_redirect
             */
            String codeUrl = "https://open.weixin.qq.com/connect/oauth2/authorize" +
                    "?appid=" + ConstantWxPropertiesUtil.WX_OPEN_APP_ID + "" +
                    "&redirect_uri=" + encode + "" +
                    "&response_type=code" +
                    "&scope=snsapi_userinfo" +   // snsapi_userinfo 是公众号的scope
                    "&state=" + uuid + "" +  // 官方说搞一个state要安全些，值可以是随机一个字符串，这里用uuid，为了方便查redis
                    "#wechat_redirect";

            map.put("codeUrl", codeUrl);
            map.put("uuid", uuid);
            return Result.ok(map);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 微信登录回调接口
    @RequestMapping("callback")
    @ResponseBody
    public Result callback(String code, String state) {
        // 这个state的值是一个uuid，可以来直接查redis
        System.out.println("state:" + state);
        //第一步 获取临时票据 code
        System.out.println("code:"+code);
        //第二步 拿着code和微信id和秘钥，请求微信固定地址 ，得到两个值
        //使用code和appid以及appscrect换取access_token
        //  %s   占位符
        StringBuffer baseAccessTokenUrl = new StringBuffer()
                .append("https://api.weixin.qq.com/sns/oauth2/access_token")
                .append("?appid=%s")
                .append("&secret=%s")
                .append("&code=%s")
                .append("&grant_type=authorization_code");
        String accessTokenUrl = String.format(baseAccessTokenUrl.toString(),
                ConstantWxPropertiesUtil.WX_OPEN_APP_ID,
                ConstantWxPropertiesUtil.WX_OPEN_APP_SECRET,
                code);
        //使用httpclient请求这个地址
        try {
            String accesstokenInfo = HttpClientUtils.get(accessTokenUrl);
            System.out.println("accesstokenInfo:"+accesstokenInfo);
            //从返回字符串获取两个值 openid  和  access_token
            JSONObject jsonObject = JSONObject.parseObject(accesstokenInfo);
            String access_token = jsonObject.getString("access_token");
            String openid = jsonObject.getString("openid");

            //判断数据库是否存在微信的扫描人信息
            //根据openid判断
            UserInfo userInfo = userInfoService.getByOpenid(openid);
            if(userInfo == null) { //数据库不存在微信信息
                //第三步 拿着openid  和  access_token请求微信地址，得到扫描人信息
                String baseUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo" +
                        "?access_token=%s" +
                        "&openid=%s";
                String userInfoUrl = String.format(baseUserInfoUrl, access_token, openid);
                String resultInfo = HttpClientUtils.get(userInfoUrl);
                System.out.println("resultInfo:"+resultInfo);
                JSONObject resultUserInfoJson = JSONObject.parseObject(resultInfo);
                //解析用户信息
                //用户昵称
                String nickname = resultUserInfoJson.getString("nickname");
                //用户头像
                String headimgurl = resultUserInfoJson.getString("headimgurl");

                //获取扫描人信息添加数据库
                userInfo = new UserInfo();
                userInfo.setNickName(nickname);
                userInfo.setOpenid(openid);
                userInfo.setStatus(1);
                userInfoService.save(userInfo);
            }
            //返回name和token字符串
            Map<String,String> map = new HashMap<>();
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getNickName();
            }
            if(StringUtils.isEmpty(name)) {
                name = userInfo.getPhone();
            }
            map.put("name", name);

            //判断userInfo是否有手机号，如果手机号为空，返回openid
            //如果手机号不为空，返回openid值是空字符串
            //前端判断：如果openid不为空，绑定手机号，如果openid为空，不需要绑定手机号
            if(StringUtils.isEmpty(userInfo.getPhone())) {
                map.put("openid", userInfo.getOpenid());
            } else {
                map.put("openid", "");
            }
            //使用jwt生成token字符串
            String token = JwtHelper.createToken(userInfo.getId(), name);
            map.put("token", token);
            // 把以uuid为key，token为value存入redis
            // 有效时间为60s
            redisTemplate.opsForValue().set(state, map, 60, TimeUnit.SECONDS);

            //返回登录成功信息
            return Result.ok("登录成功!");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail();
        }
    }

    // 前端微信登录扫码轮询接口
    @ApiOperation(value = "微信登录扫码轮询")
    @GetMapping("/getToken/{uuid}")
    @ResponseBody
    public Result getToken(@PathVariable String uuid){
        // 根据前端传来的uuid查询redis中是否有token值
        Map<String, Object> map = (Map<String, Object>) redisTemplate.opsForValue().get(uuid);
        if(map == null){
            return Result.ok();
        }
        return Result.ok(map);
    }

}
