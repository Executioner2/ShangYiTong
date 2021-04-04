package com.study.yygh;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-19 22:31
 * Versions:1.0.0
 * Description:
 */
@Data
public class UserData {
    @ExcelProperty(value = "用户编号", index = 0)
    private int uid;

    @ExcelProperty(value = "用户名称", index = 1)
    private String username;
}
