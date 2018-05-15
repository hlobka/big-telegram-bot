package helper.time;

import java.util.Calendar;

public class TimeHelper {

    public static int getHoursUntilTarget(int targetHour) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour < targetHour ? targetHour - hour : targetHour - hour + 24;
    }

}
