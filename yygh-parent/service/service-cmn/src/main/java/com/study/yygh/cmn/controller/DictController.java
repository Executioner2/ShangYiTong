package com.study.yygh.cmn.controller;

import com.study.yygh.model.cmn.Dict;
import com.study.yygh.result.Result;
import com.study.yygh.cmn.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-16 21:50
 * Versions:1.0.0
 * Description:
 */
@Api(tags = "数据字典管理")
@RestController
@RequestMapping("/admin/cmn/dict")
//@CrossOrigin // 跨域 在网关中进行了统一的跨域配置
public class DictController {
    @Resource
    private DictService dictService;

    // 根据id查询所有子节点
    @ApiOperation(value = "根据id查询所有子节点")
    @GetMapping("findChildData/{id}")
    public Result findChildData(@PathVariable Long id){
        List<Dict> dicts = dictService.findChlidData(id);
        return Result.ok(dicts);
    }

    // 导出数据字典
    @ApiOperation(value = "导出数据字典")
    @GetMapping("/exportData")
    public void exportDict(HttpServletResponse response){
        dictService.exportDictData(response);
    }

    // 导入数据字典到数据库
    @ApiOperation(value = "导入数据字典到数据库")
    @PostMapping("/importData")
    public Result importData(MultipartFile file){
        dictService.importData(file);
        return Result.ok();
    }

    // 根据dictcode和value查询
    @ApiOperation(value = "根据dictcode和value查询")
    @GetMapping("/getName/{dictcode}/{value}")
    public String getName(@PathVariable String dictcode,
                          @PathVariable String value){
        String name = dictService.getName(dictcode, value);
        return name;
    }

    // 根据value查询
    @ApiOperation(value = "根据value查询")
    @GetMapping("/getName/{value}")
    public String getName(@PathVariable String value){
        String name = dictService.getName("", value);
        return name;
    }

    // 根据dictCode获取下级节点
    @ApiOperation(value = "查询字典中医院等级")
    @GetMapping("/findByDictCode/{dictCode}")
    public Result findByDictCode(@PathVariable String dictCode){
        List<Dict> list = dictService.findByDictCode(dictCode);
        return Result.ok(list);
    }
}
