package com.mzt.logapi.starter.support.parse;

import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.beans.MethodExecuteResult;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DATE 3:32 PM
 * 解析需要存储的日志里面的SpeEL表达式
 *
 * @author mzt.
 */
public class LogRecordValueParser implements BeanFactoryAware {
    public static final String LIST_ITEM = "_l";

    private static final Pattern pattern = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");
    public static final String COMMA = ",";
    private static final Logger log = LoggerFactory.getLogger(LogRecordValueParser.class);
    private final LogRecordExpressionEvaluator expressionEvaluator = new LogRecordExpressionEvaluator();
    protected BeanFactory beanFactory;
    protected boolean diffSameWhetherSaveLog;

    private LogFunctionParser logFunctionParser;

    private DiffParseFunction diffParseFunction;

    public static int strCount(String srcText, String findText) {
        int count = 0;
        int index = 0;
        while ((index = srcText.indexOf(findText, index)) != -1) {
            index = index + findText.length();
            count++;
        }
        return count;
    }

    public String singleProcessTemplate(MethodExecuteResult methodExecuteResult,
                                        String templates,
                                        Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> stringStringMap = processTemplate(Collections.singletonList(templates), methodExecuteResult,
                beforeFunctionNameAndReturnMap);
        return stringStringMap.get(templates);
    }

    public Map<String, String> processTemplate(Collection<String> templates, MethodExecuteResult methodExecuteResult,
                                               Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> expressionValues = new HashMap<>();
        EvaluationContext evaluationContext = getEvaluationContext(methodExecuteResult);
        AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(methodExecuteResult.getMethod(), methodExecuteResult.getTargetClass());

        for (String expressionTemplate : templates) {
            if (expressionTemplate.contains("{")) {
                Matcher matcher = pattern.matcher(expressionTemplate);
                StringBuffer parsedStr = new StringBuffer();

                boolean sameDiff = false;
                while (matcher.find()) {
                    String expression = matcher.group(2);
                    String functionName = matcher.group(1);
                    if (DiffParseFunction.diffFunctionName.equals(functionName)) {
                        expression = getDiffFunctionValue(evaluationContext, annotatedElementKey, expression);
                        sameDiff = Objects.equals("", expression);
                    } else {
                        Object value = parseValueBySpel(expression, annotatedElementKey, evaluationContext);
                        expression = logFunctionParser.getFunctionReturnValue(beforeFunctionNameAndReturnMap, value, expression, functionName);
                    }
                    matcher.appendReplacement(parsedStr, Matcher.quoteReplacement(expression == null ? "" : expression));
                }
                matcher.appendTail(parsedStr);
                expressionValues.put(expressionTemplate, recordSameDiff(sameDiff, diffSameWhetherSaveLog) ? parsedStr.toString() : expressionTemplate);
            } else {
                expressionValues.put(expressionTemplate, expressionTemplate);
            }

        }
        return expressionValues;
    }

    protected Object parseValueBySpel(String expression, MethodExecuteResult methodExecuteResult) {
        EvaluationContext evaluationContext = getEvaluationContext(methodExecuteResult);
        AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(methodExecuteResult.getMethod(), methodExecuteResult.getTargetClass());
        return parseValueBySpel(expression, annotatedElementKey, evaluationContext);
    }

    private Object parseValueBySpel(String expression, AnnotatedElementKey annotatedElementKey, EvaluationContext evaluationContext) {
        return expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
    }

    private EvaluationContext getEvaluationContext(MethodExecuteResult methodExecuteResult) {
        return expressionEvaluator.createEvaluationContext(methodExecuteResult.getMethod(),
                methodExecuteResult.getArgs(), methodExecuteResult.getTargetClass(), methodExecuteResult.getResult(),
                methodExecuteResult.getErrorMsg(), beanFactory);
    }

    private boolean recordSameDiff(boolean sameDiff, boolean diffSameWhetherSaveLog) {
        if(diffSameWhetherSaveLog) {
            return true;
        }
        if(!diffSameWhetherSaveLog && sameDiff) {
            return false;
        }
        return true;
    }

    private String getDiffFunctionValue(EvaluationContext evaluationContext, AnnotatedElementKey annotatedElementKey, String expression) {
        String[] params = parseDiffFunction(expression);
        if (params.length == 1) {
            Object targetObj = parseValueBySpel(params[0], annotatedElementKey, evaluationContext);
            expression = diffParseFunction.diff(targetObj);
        } else if (params.length == 2) {
            Object sourceObj = parseValueBySpel(params[0], annotatedElementKey, evaluationContext);
            Object targetObj = parseValueBySpel(params[1], annotatedElementKey, evaluationContext);
            expression = diffParseFunction.diff(sourceObj, targetObj);
        }
        return expression;
    }

    private String[] parseDiffFunction(String expression) {
        if (expression.contains(COMMA) && strCount(expression, COMMA) == 1) {
            return expression.split(COMMA);
        }
        return new String[]{expression};
    }

    private List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> operations) {
        List<String> spElTemplates = new ArrayList<>();
        for (LogRecordOps operation : operations) {
            //执行之前的函数，失败模版不解析
            List<String> templates = getSpElTemplates(operation, operation.getSuccessLogTemplate());
            if (!CollectionUtils.isEmpty(templates)) {
                spElTemplates.addAll(templates);
            }
        }
        return spElTemplates;
    }

    protected List<String> getSpElTemplates(LogRecordOps operation, String... actions) {
        List<String> spElTemplates = new ArrayList<>();
        spElTemplates.add(operation.getType());
        spElTemplates.add(operation.getBizNo());
        spElTemplates.add(operation.getSubType());
        spElTemplates.add(operation.getExtra());
        spElTemplates.addAll(Arrays.asList(actions));
        return spElTemplates;
    }

    public List<Map<String, String>> processBeforeExecuteFunctionTemplate(LogRecordOps operation, Class<?> targetClass, Method method, Object[] args) {
        List<Map<String, String>> ret = new ArrayList<>();
        List<String> spElTemplates = getBeforeExecuteFunctionTemplate(Collections.singletonList(operation));
        AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(method, targetClass);
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, null, null, beanFactory);
        MethodExecuteResult methodExecuteResult = new MethodExecuteResult(method, args, targetClass);
        // 获取列表变量名
        String listVarName = operation.getList();
        if (!StringUtils.isEmpty(listVarName)) {
            String spelListExpression = listVarName.replaceAll("\\{", "").replaceAll("}", "");
            Object listObj = parseValueBySpel(spelListExpression, methodExecuteResult);
            if (listObj instanceof Collection) {
                Collection<?> collection = (Collection<?>) listObj;
                if (CollectionUtils.isEmpty(collection)) {
                    log.info("列表变量为空, listVarName={}", listVarName);
                    return new ArrayList<>();
                }
                for (Object obj : collection) {
                    LogRecordContext.putVariable(LIST_ITEM, obj);
                    Map<String, String> functionNameAndReturnValueMap = new HashMap<>();
                    for (String expressionTemplate : spElTemplates) {
                        functionNameAndReturnValueMap.putAll(beforeFunctionParse(expressionTemplate, annotatedElementKey, evaluationContext));
                    }
                    if (!functionNameAndReturnValueMap.isEmpty()) {
                        ret.add(functionNameAndReturnValueMap);;
                    }
                    LogRecordContext.putVariable(LIST_ITEM, null);
                }
            }
            return ret;
        }

        // 非批量
        Map<String, String> functionNameAndReturnValueMap = new HashMap<>();
        for (String expressionTemplate : spElTemplates) {
            functionNameAndReturnValueMap.putAll(beforeFunctionParse(expressionTemplate, annotatedElementKey, evaluationContext));
        }
        if (!functionNameAndReturnValueMap.isEmpty()) {
            ret.add(functionNameAndReturnValueMap);
        }
        return ret;
    }

    private Map<String, String> beforeFunctionParse(String expressionTemplate, AnnotatedElementKey annotatedElementKey, EvaluationContext evaluationContext) {
        Map<String, String> functionNameAndReturnValueMap = new HashMap<>();
        if (expressionTemplate.contains("{")) {
            Matcher matcher = pattern.matcher(expressionTemplate);
            while (matcher.find()) {
                String expression = matcher.group(2);
                if (expression.contains("#_ret") || expression.contains("#_errorMsg")) {
                    continue;
                }
                String functionName = matcher.group(1);
                if (logFunctionParser.beforeFunction(functionName)) {
                    Object value = parseValueBySpel(expression, annotatedElementKey, evaluationContext);
                    String functionReturnValue = logFunctionParser.getFunctionReturnValue(null, value, expression, functionName);
                    String functionCallInstanceKey = logFunctionParser.getFunctionCallInstanceKey(functionName, expression);
                    functionNameAndReturnValueMap.put(functionCallInstanceKey, functionReturnValue);
                }
            }
        }
        return functionNameAndReturnValueMap
    }


    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    public void setLogFunctionParser(LogFunctionParser logFunctionParser) {
        this.logFunctionParser = logFunctionParser;
    }

    public void setDiffParseFunction(DiffParseFunction diffParseFunction) {
        this.diffParseFunction = diffParseFunction;
    }
}
