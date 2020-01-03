package helper.time;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void testCheckDaysAreSame() {
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            assertThat(TimeHelper.checkDaysAreSame(dayOfWeek, DayOfWeek.of(dayOfWeek.ordinal() + 1))).isTrue();
        }
    }

    @Test
    public void testCheckToDayIs() {
        assertThat(TimeHelper.checkToDayIs(LocalDate.now().getDayOfWeek())).isTrue();
    }

    @Test
    public void getMinutesAsStringTimeTest() {
        assertThat(TimeHelper.getMinutesAsStringTime(1)).isEqualTo("0d 0h 1m");
        assertThat(TimeHelper.getMinutesAsStringTime(60)).isEqualTo("0d 1h 0m");
        assertThat(TimeHelper.getMinutesAsStringTime(61)).isEqualTo("0d 1h 1m");
        assertThat(TimeHelper.getMinutesAsStringTime(60 * 24)).isEqualTo("1d 0h 0m");
        assertThat(TimeHelper.getMinutesAsStringTime(60 * 24 + 1)).isEqualTo("1d 0h 1m");
        assertThat(TimeHelper.getMinutesAsStringTime(60 * 24 + 1 + 60)).isEqualTo("1d 1h 1m");
        assertThat(TimeHelper.getMinutesAsStringTime(60 * 24 + 60)).isEqualTo("1d 1h 0m");
    }
}