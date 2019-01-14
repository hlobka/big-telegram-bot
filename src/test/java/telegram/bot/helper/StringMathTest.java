package telegram.bot.helper;

import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static telegram.bot.helper.StringMath.stringToMathResult;

public class StringMathTest {

    @Test
    public void testREG_EXPRESSION_TO_MATH_MATCH() {
        assertThat(("-1*-1")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("1*-5")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("1+1")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("1+1+1+1+1+1+1+11+1+11+1+11+1+11+1+1+1+1+1+11+1+1+1+1+1+1+1+1+1+1+1+1")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("10+-10")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("10-10+10-10+10-10+10-10+10-10+10-10")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("10+10-10+10-10+10-10+10-10+10-10+10-10")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("1+1+1+1+1-2+1+1+1+1")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("(((1.5+1.5))/(1.0*1.0))/2.5")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("(10+10)/2+2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("2+(10+10)/2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("(2+2+(10+10)/2)/2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("(((1+1))/(1*1))/2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("((1+1)/(1*1))/2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("(((499+1)/(5*10))/(10-5))")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("1+1-2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("1+ 1- 2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("2+2/2")).matches(StringMath.REG_EXPRESSION_TO_MATH_MATCH);

        assertThat(("))))))")).doesNotMatch(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("12.30")).doesNotMatch(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("12,30")).doesNotMatch(StringMath.REG_EXPRESSION_TO_MATH_MATCH);
        assertThat(("12,30")).doesNotMatch(StringMath.REG_EXPRESSION_TO_MATH_MATCH);

    }

    @Test(expectedExceptions = NumberFormatException.class)
    public void testStringToMathResultNegativeTest() {
        stringToMathResult("((((1+1))/(1*1))/2");
    }

    @Test
    public void testStringToMathResult() {
        assertThat(stringToMathResult("-1.2+-3")).isEqualTo(-4.2);
        assertThat(stringToMathResult("-1.2+3")).isEqualTo(1.8);
        assertThat(stringToMathResult("-1*-1")).isEqualTo(1.0);
        assertThat(stringToMathResult("1*-5")).isEqualTo(-5.0);
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