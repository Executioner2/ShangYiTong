package com.study.yygh.oss.api;

import com.study.yygh.oss.service.FileService;
import com.study.yygh.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-30 21:30
 * Versions:1.0.0
 * Description:
 */
@RestController
@Api(tags = "文件管理模块")
@RequestMapping("/api/oss/file")
public class FileApiController {
    @Resource
    private FileService fileService;

    // 上传文件到阿里云并返回文件路径
    @ApiOperation(value = "上传文件到阿里云")
    @PostMapping("/fileUpload")
    public Result fileUpload(MultipartFile file){
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
