package com.study.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 21:31
 * Versions:1.0.0
 * Description:
 */
public interface FileService {
    // 获取上传文件
    String upload(MultipartFile file);
}
