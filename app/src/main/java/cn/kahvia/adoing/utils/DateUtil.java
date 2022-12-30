package cn.kahvia.adoing.utils;

import java.util.Calendar;

public class DateUtil {
    private static Calendar calendar=Calendar.getInstance();

    public static int year(){
        return calendar.get(Calendar.YEAR);
    }

    public static int month(){
        return calendar.get(Calendar.MONTH)+1;
    }

    public static int day(){
        return calendar.get(Calendar.DATE);
    }
}
