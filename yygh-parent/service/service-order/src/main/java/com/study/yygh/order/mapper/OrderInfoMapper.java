package com.study.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.study.yygh.model.order.OrderInfo;
import com.study.yygh.vo.order.OrderCountQueryVo;
import com.study.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 22:22
 * Versions:1.0.0
 * Description:
 */
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    // 按预约日期分组统计预约数量
    List<OrderCountVo> selectOrderCount(@Param("vo")OrderCountQueryVo orderCountQueryVo);
}
