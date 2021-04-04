package com.study.yygh.cmn.client;

import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-22 22:39
 * Versions:1.0.0
 * Description: 调用CMN服务提供者
 */
@FeignClient(value = "service-cmn")
@Component // 交给spring容器管理
public interface DictFeignClient {

    // 根据dictcode和value查询
    @ApiOperation(value = "根据dictcode和value查询")
    @GetMapping("/admin/cmn/dict/getName/{dictcode}/{value}")
    String getName(@PathVariable("dictcode") String dictcode,
                          @PathVariable("value") String value);

    // 根据value查询
    @ApiOperation(value = "根据value查询")
    @GetMapping("/admin/cmn/dict/getName/{value}")
    String getName(@PathVariable("value") String value);
}
