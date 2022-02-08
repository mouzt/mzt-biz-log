package com.mzt.logserver.diff;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * @author muzhantong
 * create on 2022/1/3 7:22 下午
 */
public class DiffFunction {

    public static String reverseString(Object o1, Object o2) {
        return o1.toString().concat("###").concat(o2.toString());
    }

    public static void main(String[] args) throws NoSuchMethodException {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        context.registerFunction("reverseString",
                DiffFunction.class.getMethod("reverseString",
                        Object.class, Object.class));
        String helloWorldReversed =
                parser.parseExpression("#reverseString('hello', 123)").getValue(context, String.class);
        System.out.println(helloWorldReversed);

    }
}
