package helper.time;

import org.testng.annotations.Test;

import java.util.Calendar;

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
}