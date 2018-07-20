package helper.time;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class TimeHelper {

    public static int getHoursUntilTarget(int targetHour) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        while (hour >= targetHour){
            hour -= 24;
        }
        return targetHour - hour;
    }

    public static Long getMinutesUntilTargetHour(int targetHour) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        while (hour >= targetHour){
            hour -= 24;
        }
        return TimeUnit.HOURS.toMinutes(targetHour - hour) + minutes;
    }

    public static boolean checkToDayIs(DayOfWeek dayOfWeek) {
        return checkDaysAreSame(dayOfWeek, LocalDate.now().getDayOfWeek());
    }

    protected static boolean checkDaysAreSame(DayOfWeek dayOfWeek1, DayOfWeek dayOfWeek2) {
        return dayOfWeek1 == dayOfWeek2;
    }

    public static void waitTime(int timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getCurrentTimeStamp() {
        return getCurrentTimeStamp("yyyy-MM-dd HH:mm:ss.SSS");
    }

    public static String getCurrentTimeStamp(String pattern) {
        return new SimpleDateFormat(pattern).format(new Date());
    }
}
