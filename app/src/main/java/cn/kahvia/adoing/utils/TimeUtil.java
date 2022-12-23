package cn.kahvia.adoing.utils;

public class TimeUtil {
    public static String formatTime(int time){
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;

        String string_hours=formatPart(hours);
        String string_minutes=formatPart(minutes);
        String string_seconds=formatPart(seconds);

        return string_hours+":"+string_minutes+":"+string_seconds;
    }

    public static String formatPart(int part){
        if (part<10)
            return "0"+part;
        else
            return ""+part;
    }
}
