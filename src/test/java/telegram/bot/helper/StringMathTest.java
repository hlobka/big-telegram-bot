package telegram.bot.helper;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static telegram.bot.helper.StringMath.stringToMathResult;

public class StringMathTest {
    @Test(expectedExceptions = NumberFormatException.class)
    public void testStringToMathResultNegativeTest() {
        stringToMathResult("((((1+1))/(1*1))/2");
    }

    @Test
    public void testStringToMathResult() {
        assertThat(stringToMathResult("1+1")).isEqualTo(2.0);
        assertThat(stringToMathResult("1+1+1+1+1+1+1+11+1+11+1+11+1+11+1+1+1+1+1+11+1+1+1+1+1+1+1+1+1+1+1+1")).isEqualTo(82);
        assertThat(stringToMathResult("10+-10")).isEqualTo(0);
        assertThat(stringToMathResult("10-10+10-10+10-10+10-10+10-10+10-10")).isEqualTo(0);
        assertThat(stringToMathResult("10+10-10+10-10+10-10+10-10+10-10+10-10")).isEqualTo(10);

        assertThat(stringToMathResult("1+1+1+1+1-2+1+1+1+1")).isEqualTo(7);

        assertThat(stringToMathResult("(((1.5+1.5))/(1.0*1.0))/2.5")).isEqualTo(1.2);
        assertThat(stringToMathResult("(10+10)/2+2")).isEqualTo(12.0);
        assertThat(stringToMathResult("2+(10+10)/2")).isEqualTo(12.0);
        assertThat(stringToMathResult("(2+2+(10+10)/2)/2")).isEqualTo(7.0);
        assertThat(stringToMathResult("(((1+1))/(1*1))/2")).isEqualTo(1.0);
        assertThat(stringToMathResult("((1+1)/(1*1))/2")).isEqualTo(1.0);
        assertThat(stringToMathResult("(((499+1)/(5*10))/(10-5))")).isEqualTo(2.0);
        assertThat(stringToMathResult("1+1-2")).isEqualTo(0.0);
        assertThat(stringToMathResult("1+ 1- 2")).isEqualTo(0.0);
        assertThat(stringToMathResult("2+2/2")).isEqualTo(3.0);
    }
}