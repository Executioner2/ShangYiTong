import org.joda.time.DateTime;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyright@1205878539@qq.com
 * Author:2Executioner
 * Date:2021-03-31 19:42
 * Versions:1.0.0
 * Description:
 */
@SpringBootTest
public class Test01 {
    @Test
    public void testTime(){
        // 往后推算一天
        DateTime curDateTime = new DateTime().plusDays(1);
        String dateString = curDateTime.toString("yyyy-MM-dd");
        System.out.println(dateString);
        System.out.println(new DateTime(dateString).toDate()); // 转换为只有日期时间为0的DateTime
        System.out.println(curDateTime.toDate());
    }

    @Test
    public void test01(){
        int cycle = 10;
        List<Date> dateList = new ArrayList<>();
        for (int i = 0; i < cycle; i++) {
            //计算当前预约日期
            DateTime curDateTime = new DateTime().plusDays(i);
            String dateString = curDateTime.toString("yyyy-MM-dd");
            dateList.add(new DateTime(dateString).toDate());
        }
        System.out.println(dateList);
    }
}
