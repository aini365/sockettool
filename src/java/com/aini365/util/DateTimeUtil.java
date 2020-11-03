package com.aini365.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    public static String getNowTime(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 日期时间转字符串
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
}
}
