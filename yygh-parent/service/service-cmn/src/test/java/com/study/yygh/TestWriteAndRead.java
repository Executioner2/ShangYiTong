package com.study.yygh;

import com.alibaba.excel.EasyExcel;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-19 22:39
 * Versions:1.0.0
 * Description:
 */
@SpringBootTest
public class TestWriteAndRead {
    @Test
    public void writeTest() {
        // 构建一个list集合
        List<UserData> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            UserData userData = new UserData();
            userData.setUid(i);
            userData.setUsername("lisi" + i);
            list.add(userData);
        }

        // 设置excel文件路径
        String file_name = "E:\\test\\01.xlsx";

        // 调用方法实现写操作
        EasyExcel.write(file_name, UserData.class).sheet("测试")
                .doWrite(list);
    }

    @Test
    public void readTest(){
        // 设置excel文件路径
        String file_name = "E:\\test\\01.xlsx";
        // 调用方法实现读操作
        EasyExcel.read(file_name, UserData.class, new ExcelListener()).sheet().doRead();
    }
}
