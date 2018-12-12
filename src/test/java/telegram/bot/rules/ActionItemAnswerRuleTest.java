package telegram.bot.rules;

import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class ActionItemAnswerRuleTest {
    @Test
    public void testGetDaysOfMonth_12_2018() {
        List<List<Integer>> expectedDaysOfMonth = Arrays.asList(
                Arrays.asList(-1, -1, -1, -1, -1, 1, 2),
                Arrays.asList(3, 4, 5, 6, 7, 8, 9),
                Arrays.asList(10, 11, 12, 13, 14, 15, 16),
                Arrays.asList(17, 18, 19, 20, 21, 22, 23),
                Arrays.asList(24, 25, 26, 27, 28, 29, 30),
                Arrays.asList(31, -1, -1, -1, -1, -1, -1)
        );
        List<List<Integer>> daysOfMonth = ActionItemAnswerRule.getDaysOfMonth(12, 2018);
        Assertions.assertThat(daysOfMonth).isEqualTo(expectedDaysOfMonth);
    }

    @Test
    public void testGetDaysOfMonth_1_2019() {
        List<List<Integer>> expectedDaysOfMonth = Arrays.asList(
                Arrays.asList(-1, 1, 2, 3, 4, 5, 6),
                Arrays.asList(7, 8, 9, 10, 11, 12, 13),
                Arrays.asList(14, 15, 16, 17, 18, 19, 20),
                Arrays.asList(21, 22, 23, 24, 25, 26, 27),
                Arrays.asList(28, 29, 30, 31, -1, -1, -1)
        );
        List<List<Integer>> daysOfMonth = ActionItemAnswerRule.getDaysOfMonth(1, 2019);
        Assertions.assertThat(daysOfMonth).isEqualTo(expectedDaysOfMonth);
    }
}