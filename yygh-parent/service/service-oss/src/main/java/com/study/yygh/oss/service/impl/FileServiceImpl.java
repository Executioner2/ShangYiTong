package com.study.yygh.oss.service.impl;

import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.study.yygh.oss.service.FileService;
import com.study.yygh.oss.utils.ConstantOssPropertiesUtil;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 21:32
 * Versions:1.0.0
 * Description:
 */
@Service
public class FileServiceImpl implements FileService {
    /**
     * 获取上传文件
     * @param file
     * @return
     */
    @Override
    public String upload(MultipartFile file) {
        // 连接信息
        String accessKeyId = ConstantOssPropertiesUtil.ACCESS_KEY_ID;
        String bucketName = ConstantOssPropertiesUtil.BUCKET;
        String endpoint = ConstantOssPropertiesUtil.ENDPOINT;
        String accessKeySecret = ConstantOssPropertiesUtil.SECRECT;
        try {
            // 创建ossClient实例
            OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
            // 获取上传文件的名字
            String filename = file.getOriginalFilename();
            // 生成uuid避免oss中文件名重复
            String uuid = IdUtil.randomUUID().replaceAll("-", "");
            filename = uuid + filename;
            // 按日期生成文件夹
            String timeUrl = new DateTime().toString("yyyy/MM/dd");
            filename = timeUrl + "/" + filename;
            // 获取文件input流
            InputStream inputStream = file.getInputStream();
            // 调用方法实现上传
            ossClient.putObject(bucketName, filename, inputStream);
            // 关闭ossClient
            ossClient.shutdown();
            // 文件上传之后的路径
            String url = "https://" + bucketName + "." + endpoint + "/" + filename;
            // 返回文件访问路径
            return url;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
