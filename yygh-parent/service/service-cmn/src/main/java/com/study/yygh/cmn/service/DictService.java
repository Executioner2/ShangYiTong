package com.study.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.study.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-16 21:46
 * Versions:1.0.0
 * Description:
 */
public interface DictService extends IService<Dict> {
    // 根据数据id查询子数据列表
    List<Dict> findChildData(Long id);

    void exportDictData(HttpServletResponse response);

    void importData(MultipartFile file);

    // 根据dictCode查询数据
    List<Dict> findByDictCode(String dictCode);

    // 根据dictcode和value查询字典中的name
    String getName(String dictcode, String value);
}
