package telegram.bot.helper;

import helper.string.StringHelper;

public class StringMath {
    public static final String MATH_REG = "^(-?\\d+(\\.\\d+)?)( ?)+%s( ?)+(-?\\d+(\\.\\d+)?)$";
    public static final String MATH_PLUS = String.format(MATH_REG, "\\+");
    public static final String MATH_MINUS = String.format(MATH_REG, "\\-");
    public static final String MATH_MULTIPLY = String.format(MATH_REG, "\\*");
    public static final String MATH_DIVIDE = String.format(MATH_REG, "\\/");

    public static double stringToMathResult(String mathExpression) {
        mathExpression = mathExpression.replaceAll("\\s", "");
        mathExpression = validateMathExpression(mathExpression);
        String regexp = "(\\(([\\d+-/*^]+)=?\\))";
        while (StringHelper.hasRegString(mathExpression, regexp, 2)) {
            String regString = StringHelper.getRegString(mathExpression, regexp, 2);
            String replacement = stringToMathResult(regString) + "";
            mathExpression = mathExpression.replace("("+regString+")", replacement);
//            mathExpression = mathExpression.replace("("+replacement+")", replacement);

            /*if(StringHelper.hasRegString(mathExpression, "\\((\\d\\.\\d)\\)", 1)){
                String regString = StringHelper.getRegString(mathExpression, "\\((\\d\\.\\d)\\)", 1);
                mathExpression = mathExpression.replace(regString, regString + "");
            }*/
            mathExpression = validateMathExpression(mathExpression);
        }
        if (mathExpression.matches(MATH_PLUS)) {
            return getRegStringAsFloat(mathExpression, MATH_PLUS, 1) + getRegStringAsFloat(mathExpression, MATH_PLUS, 5);
        }
        if (mathExpression.matches(MATH_MINUS)) {
            return getRegStringAsFloat(mathExpression, MATH_MINUS, 1) - getRegStringAsFloat(mathExpression, MATH_MINUS, 5);
        }
        if (mathExpression.matches(MATH_MULTIPLY)) {
            return getRegStringAsFloat(mathExpression, MATH_MULTIPLY, 1) * getRegStringAsFloat(mathExpression, MATH_MULTIPLY, 5);
        }
        if (mathExpression.matches(MATH_DIVIDE)) {
            return getRegStringAsFloat(mathExpression, MATH_DIVIDE, 1) / getRegStringAsFloat(mathExpression, MATH_DIVIDE, 5);
        }
//        mathExpression.split("(\\(.*\\)).*");
        return Double.parseDouble(mathExpression);
    }

    public static String validateMathExpression(String mathExpression) {
        mathExpression = mathExpression.replaceAll("\\+-", "-");
        if(!mathExpression.contains("(") && !mathExpression.matches("(\\d+(\\.\\d+)?[-+*/^]\\d+(\\.\\d+)?)")) {
//
//            mathExpression = mathExpression.replaceAll("(^\\d+(\\.\\d+)?[-+*/^]\\d+(\\.\\d+)?)", "($1)");
            mathExpression = mathExpression.replaceAll("(\\d+(\\.\\d+)?[*/^]\\d+(\\.\\d+)?)", "($1)");
            mathExpression = mathExpression.replaceAll("(\\d+(\\.\\d+)?[-]\\d+(\\.\\d+)?)", "($1)");
            mathExpression = mathExpression.replaceAll("(\\d+(\\.\\d+)?[+]\\d+(\\.\\d+)?)", "($1)");
        }
        return mathExpression;
    }

    private static double getRegStringAsFloat(String mathExpression, String regExp, int group) {
        return Double.parseDouble(StringHelper.getRegString(mathExpression, regExp, group));
    }
}
