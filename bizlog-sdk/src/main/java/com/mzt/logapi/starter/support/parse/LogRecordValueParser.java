package com.mzt.logapi.starter.support.parse;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.mzt.logapi.service.IFunctionService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DATE 3:32 PM
 * 解析需要存储的日志里面的SpeEL表达式
 *
 * @author mzt.
 */
public class LogRecordValueParser implements BeanFactoryAware {

    protected BeanFactory beanFactory;

    private LogRecordExpressionEvaluator expressionEvaluator = new LogRecordExpressionEvaluator();
    private IFunctionService functionService;
    private static Pattern pattern = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");


    public Map<String, String> processTemplate(Collection<String> templates, Object ret, Class<?> targetClass, Method method, Object[] args, String errorMsg) {
        Map<String, String> expressionValues = Maps.newHashMap();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, ret, errorMsg, beanFactory);

        for (String expressionTemplate : templates) {
            if (expressionTemplate.contains("{{") || expressionTemplate.contains("{")) {
                Matcher matcher = pattern.matcher(expressionTemplate);
                StringBuffer parsedStr = new StringBuffer();
                while (matcher.find()) {
                    String expression = matcher.group(2);
                    AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(method, targetClass);
                    String value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
                    String functionName = matcher.group(1);
                    matcher.appendReplacement(parsedStr, Strings.nullToEmpty(functionService.apply(functionName, value)));
                }
                matcher.appendTail(parsedStr);
                expressionValues.put(expressionTemplate, parsedStr.toString());
            } else {
                expressionValues.put(expressionTemplate, expressionTemplate);
            }

        }
        return expressionValues;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setFunctionService(IFunctionService functionService) {
        this.functionService = functionService;
    }
}
