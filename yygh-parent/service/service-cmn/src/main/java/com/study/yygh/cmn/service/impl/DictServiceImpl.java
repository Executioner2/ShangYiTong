package com.study.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.study.yygh.cmn.listener.DictListener;
import com.study.yygh.cmn.mapper.DictMapper;
import com.study.yygh.model.cmn.Dict;
import com.study.yygh.cmn.service.DictService;
import com.study.yygh.vo.cmn.DictEeVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-19 19:24
 * Versions:1.0.0
 * Description:
 */
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    /**
     * 指定id下的查询所有子节点
     * @param id
     * @return
     */
    @Override
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")  // 使用Spring Cache，底层存储到了redis中去
    public List<Dict> findChlidData(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper();
        wrapper.eq("parent_id", id);
        List<Dict> dicts = baseMapper.selectList(wrapper);
        for (Dict dict : dicts) {
            Boolean flag = this.isChildren(dict.getId());
            dict.setHasChildren(flag);
        }
        return dicts;
    }

    /**
     * 导出数据字典
     * @param response
     */
    @Override
    public void exportDictData(HttpServletResponse response) {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        try {
            String fileName = URLEncoder.encode("数据字典", "utf-8");
            // 参数1：设置头信息以下载方式打开
            // 参数2：文件名
            response.setHeader("Content-disposition", "attachment;filename="+ fileName + ".xlsx");
            // 查询数据库
            List<Dict> dicts = baseMapper.selectList(null);
            List<DictEeVo> dictEeVos = new ArrayList<>();
            for (Dict dict : dicts) {
                DictEeVo dictEeVo = new DictEeVo();
                BeanUtils.copyProperties(dict, dictEeVo);
                dictEeVos.add(dictEeVo);
            }
            EasyExcel.write(response.getOutputStream(), DictEeVo.class).sheet("dict").doWrite(dictEeVos);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 导入数据字典
     * @param file
     */
    @Override
    @CacheEvict(value = "dict", allEntries = true) // 添加操作，清空缓存
    public void importData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictEeVo.class, new DictListener(baseMapper)).sheet().doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据DictCode查询子数据
     * @param dictCode
     * @return
     */
    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict parentDict = this.getParentDictByDictcode(dictCode);
        List<Dict> dicts = baseMapper.selectList(new QueryWrapper<Dict>().eq("parent_id", parentDict.getId()));
        return dicts;
    }

    /**
     * 根据dictcode和value查询字典中的name
     * @param dictcode
     * @param value
     * @return
     */
    @Override
    public String getName(String dictcode, String value) {
        Dict dict = null;

        if (StringUtils.isEmpty(dictcode)) { // 如果不为空则根据value查询
            dict = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("value", value));
        }else{ // 否则就根据dictcode和value查询
            // 先查询出父Dict
            Dict parentDict = this.getParentDictByDictcode(dictcode);
            // 再查询根据parent_id和value查询dict
            dict = baseMapper.selectOne(new QueryWrapper<Dict>()
                    .eq("parent_id", parentDict.getId())
                    .eq("value", value));
        }

        // dict如果为空返回null
        if (dict == null) {
            return null;
        }

        // 否则返回dict的name属性值
        return dict.getName();
    }

    /**
     * 根据dictcode查询dict
     * @param dictcode
     * @return
     */
    private Dict getParentDictByDictcode(String dictcode) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("dict_code", dictcode);
        Dict parentDict = baseMapper.selectOne(wrapper);
        return parentDict;
    }

    /**
     * 判断一个节点下是否有子节点
     * @param id
     * @return 如果查询结果大于0表示有子节点，否则没有
     */
    private Boolean isChildren(Long id){
        QueryWrapper<Dict> wrapper = new QueryWrapper();
        wrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }
}
