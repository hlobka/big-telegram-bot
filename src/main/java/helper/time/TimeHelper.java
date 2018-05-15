package helper.time;

import java.util.Calendar;
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
}
