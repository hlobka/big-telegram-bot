package telegram.bot.helper;

import helper.string.StringHelper;

public class StringMath {
    public static final String REG_EXPRESSION_TO_MATH_MATCH = "^-?\\d+(\\.\\d+)?(( +)?[-+*/^]( +)?-?\\d+(\\.\\d+)?)+$|^[0-9+ \\-/*^)(.]{7,}$";
    public static final String MATH_REG = "^(-?\\d+(\\.\\d+)?)( ?)+%s( ?)+(-?\\d+(\\.\\d+)?)$";
    public static final String MATH_PLUS = String.format(MATH_REG, "\\+");
    public static final String MATH_MINUS = String.format(MATH_REG, "\\-");
    public static final String MATH_MULTIPLY = String.format(MATH_REG, "\\*");
    public static final String MATH_DIVIDE = String.format(MATH_REG, "\\/");

    public static double stringToMathResult(final String mathExpression) {
        String mathExpressionAsResult = mathExpression.replaceAll("\\s", "");
        mathExpressionAsResult = validateMathExpression(mathExpressionAsResult);
        String regexp = "(\\(([\\d+-/*^]+)=?\\))";
        while (StringHelper.hasRegString(mathExpressionAsResult, regexp, 2)) {
            String regString = StringHelper.getRegString(mathExpressionAsResult, regexp, 2);
            String replacement = stringToMathResult(regString) + "";
            mathExpressionAsResult = mathExpressionAsResult.replace("("+regString+")", replacement);
            mathExpressionAsResult = validateMathExpression(mathExpressionAsResult);
        }
        if (mathExpressionAsResult.matches(MATH_PLUS)) {
            return getRegStringAsFloat(mathExpressionAsResult, MATH_PLUS, 1) + getRegStringAsFloat(mathExpressionAsResult, MATH_PLUS, 5);
        }
        if (mathExpressionAsResult.matches(MATH_MINUS)) {
            return getRegStringAsFloat(mathExpressionAsResult, MATH_MINUS, 1) - getRegStringAsFloat(mathExpressionAsResult, MATH_MINUS, 5);
        }
        if (mathExpressionAsResult.matches(MATH_MULTIPLY)) {
            return getRegStringAsFloat(mathExpressionAsResult, MATH_MULTIPLY, 1) * getRegStringAsFloat(mathExpressionAsResult, MATH_MULTIPLY, 5);
        }
        if (mathExpressionAsResult.matches(MATH_DIVIDE)) {
            return getRegStringAsFloat(mathExpressionAsResult, MATH_DIVIDE, 1) / getRegStringAsFloat(mathExpressionAsResult, MATH_DIVIDE, 5);
        }
        return Double.parseDouble(mathExpressionAsResult);
    }

    public static String validateMathExpression(String mathExpression) {
        mathExpression = mathExpression.replaceAll("\\+-", "-");
        if(!mathExpression.contains("(") && !mathExpression.matches("(-?\\d+(\\.\\d+)?[-+*/^]-?\\d+(\\.\\d+)?)")) {
            mathExpression = mathExpression.replaceAll("(-?\\d+(\\.\\d+)?[*/^]-?\\d+(\\.\\d+)?)", "($1)");
            mathExpression = mathExpression.replaceAll("(-?\\d+(\\.\\d+)?[-]-?\\d+(\\.\\d+)?)", "($1)");
            mathExpression = mathExpression.replaceAll("(-?\\d+(\\.\\d+)?[+]-?\\d+(\\.\\d+)?)", "($1)");
        }
        return mathExpression;
    }

    private static double getRegStringAsFloat(String mathExpression, String regExp, int group) {
        return Double.parseDouble(StringHelper.getRegString(mathExpression, regExp, group));
    }
}
