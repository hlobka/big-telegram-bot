package helper.time;

import org.testng.annotations.Test;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.testng.Assert.*;

public class TimeHelperTest {

    @Test
    public void testGetHoursUntilTarget() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        assertEquals(24, TimeHelper.getHoursUntilTarget(hour));
        assertEquals(1, TimeHelper.getHoursUntilTarget(hour + 1));
        assertEquals(23, TimeHelper.getHoursUntilTarget(hour - 1));
    }

    @Test
    public void testGetHoursUntilTargetByUsingWrongTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        assertEquals(48, TimeHelper.getHoursUntilTarget(hour + 24 * 2));
        assertEquals(TimeHelper.getHoursUntilTarget(hour), TimeHelper.getHoursUntilTarget(hour - 24 * 2));
    }

    @Test
    public void testGetHoursUntilTargetWorksWellForAllAvailableTimes() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        for (int i = 1; i <= 24; i++) {
            int targetHour = hour + i;
            if (targetHour > 24) {
                targetHour -= 24;
            }
            assertEquals(i, TimeHelper.getHoursUntilTarget(targetHour));
        }
    }

    @Test
    public void testGetMinutesUntilTargetHour() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        TimeUnit hours = TimeUnit.HOURS;
        Function<Long, Long> hoursToMinutes = aHour -> minutes + hours.toMinutes(aHour);
        assertEquals(hoursToMinutes.apply(24L), TimeHelper.getMinutesUntilTargetHour(hour));
        assertEquals(hoursToMinutes.apply(1L), TimeHelper.getMinutesUntilTargetHour(hour + 1));
        assertEquals(hoursToMinutes.apply(23L), TimeHelper.getMinutesUntilTargetHour(hour - 1));
    }

    @Test
    public void testGetMinutesUntilTargetHourByUsingWrongTime() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        TimeUnit hours = TimeUnit.HOURS;
        Function<Long, Long> hoursToMinutes = aHour -> minutes + hours.toMinutes(aHour);
        assertEquals(hoursToMinutes.apply(48L), TimeHelper.getMinutesUntilTargetHour(hour + 24 * 2));
        assertEquals(TimeHelper.getHoursUntilTarget(hour), TimeHelper.getHoursUntilTarget(hour - 24 * 2));
    }

    @Test
    public void testGetMinutesUntilTargetHourWorksWellForAllAvailableTimes() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        for (long i = 1; i <= 24; i++) {
            long targetHour = hour + i;
            if (targetHour > 24) {
                targetHour -= 24;
            }
            int minutes = calendar.get(Calendar.MINUTE);
            TimeUnit hours = TimeUnit.HOURS;
            Function<Long, Long> hoursToMinutes = aHour -> minutes + hours.toMinutes(aHour);
            assertEquals(hoursToMinutes.apply(i), TimeHelper.getMinutesUntilTargetHour((int) targetHour));
        }
    }
}