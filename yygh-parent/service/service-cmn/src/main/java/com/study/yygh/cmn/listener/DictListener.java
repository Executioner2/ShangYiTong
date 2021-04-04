package com.study.yygh.cmn.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.study.yygh.cmn.mapper.DictMapper;
import com.study.yygh.model.cmn.Dict;
import com.study.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-20 12:28
 * Versions:1.0.0
 * Description:
 */
public class DictListener extends AnalysisEventListener<DictEeVo> {
    private DictMapper dictMapper;

    public DictListener(DictMapper dictMapper){
        this.dictMapper = dictMapper;
    }

    // 从第二行开始，一行一行的读取
    @Override
    public void invoke(DictEeVo dictEeVo, AnalysisContext analysisContext) {
        Dict dict = new Dict();
        BeanUtils.copyProperties(dictEeVo, dict);
        dictMapper.insert(dict);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
