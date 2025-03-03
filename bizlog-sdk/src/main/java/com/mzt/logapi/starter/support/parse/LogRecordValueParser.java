package com.mzt.logapi.starter.support.parse;

import com.mzt.logapi.beans.MethodExecuteResult;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
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

    private static final Pattern pattern = Pattern.compile("\\{\\s*(\\w*)\\s*\\{(.*?)}}");
    public static final String COMMA = ",";
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
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(methodExecuteResult.getMethod(),
                methodExecuteResult.getArgs(), methodExecuteResult.getTargetClass(), methodExecuteResult.getResult(),
                methodExecuteResult.getErrorMsg(), beanFactory);

        for (String expressionTemplate : templates) {
            if (expressionTemplate.contains("{")) {
                Matcher matcher = pattern.matcher(expressionTemplate);
                StringBuffer parsedStr = new StringBuffer();
                AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(methodExecuteResult.getMethod(), methodExecuteResult.getTargetClass());
                boolean sameDiff = false;
                while (matcher.find()) {
                    String expression = matcher.group(2);
                    String functionName = matcher.group(1);
                    if (DiffParseFunction.diffFunctionName.equals(functionName)) {
                        expression = getDiffFunctionValue(evaluationContext, annotatedElementKey, expression);
                        sameDiff = Objects.equals("", expression);
                    } else {
                        Object value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
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

    private boolean recordSameDiff(boolean sameDiff, boolean diffSameWhetherSaveLog) {
        if(diffSameWhetherSaveLog == true) {
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
            Object targetObj = expressionEvaluator.parseExpression(params[0], annotatedElementKey, evaluationContext);
            expression = diffParseFunction.diff(targetObj);
        } else if (params.length == 2) {
            Object sourceObj = expressionEvaluator.parseExpression(params[0], annotatedElementKey, evaluationContext);
            Object targetObj = expressionEvaluator.parseExpression(params[1], annotatedElementKey, evaluationContext);
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

    public Map<String, String> processBeforeExecuteFunctionTemplate(Collection<String> templates, Class<?> targetClass, Method method, Object[] args) {
        Map<String, String> functionNameAndReturnValueMap = new HashMap<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(method, args, targetClass, null, null, beanFactory);

        for (String expressionTemplate : templates) {
            if (expressionTemplate.contains("{")) {
                Matcher matcher = pattern.matcher(expressionTemplate);
                while (matcher.find()) {
                    String expression = matcher.group(2);
                    if (expression.contains("#_ret") || expression.contains("#_errorMsg")) {
                        continue;
                    }
                    AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(method, targetClass);
                    String functionName = matcher.group(1);
                    if (logFunctionParser.beforeFunction(functionName)) {
                        Object value = expressionEvaluator.parseExpression(expression, annotatedElementKey, evaluationContext);
                        String functionReturnValue = logFunctionParser.getFunctionReturnValue(null, value, expression, functionName);
                        String functionCallInstanceKey = logFunctionParser.getFunctionCallInstanceKey(functionName, expression);
                        functionNameAndReturnValueMap.put(functionCallInstanceKey, functionReturnValue);
                    }
                }
            }
        }
        return functionNameAndReturnValueMap;
    }

    public List<LogRecord> processLogRecordOps(List<LogRecordOps> logRecordOpsList, MethodExecuteResult methodExecuteResult,
                                               Map<String, String> beforeFunctionNameAndReturnMap) {
        List<LogRecord> logRecords = new ArrayList<>();
        for (LogRecordOps logRecordOps : logRecordOpsList) {
            if (StringUtils.isEmpty(logRecordOps.getList())) {
                logRecords.add(processSingleLogRecordOps(logRecordOps, methodExecuteResult, beforeFunctionNameAndReturnMap));
            } else {
                logRecords.addAll(processListLogRecordOps(logRecordOps, methodExecuteResult, beforeFunctionNameAndReturnMap));
            }
        }
        return logRecords;
    }

    private LogRecord processSingleLogRecordOps(LogRecordOps logRecordOps, MethodExecuteResult methodExecuteResult,
                                                Map<String, String> beforeFunctionNameAndReturnMap) {
        Map<String, String> expressionValues = processTemplate(getSpElTemplates(logRecordOps), methodExecuteResult, beforeFunctionNameAndReturnMap);
        return buildLogRecord(logRecordOps, expressionValues, methodExecuteResult.getMethod());
    }

    private List<LogRecord> processListLogRecordOps(LogRecordOps logRecordOps, MethodExecuteResult methodExecuteResult,
                                                    Map<String, String> beforeFunctionNameAndReturnMap) {
        List<LogRecord> logRecords = new ArrayList<>();
        EvaluationContext evaluationContext = expressionEvaluator.createEvaluationContext(methodExecuteResult.getMethod(),
                methodExecuteResult.getArgs(), methodExecuteResult.getTargetClass(), methodExecuteResult.getResult(),
                methodExecuteResult.getErrorMsg(), beanFactory);
        AnnotatedElementKey annotatedElementKey = new AnnotatedElementKey(methodExecuteResult.getMethod(), methodExecuteResult.getTargetClass());
        Object listObject = expressionEvaluator.parseExpression(logRecordOps.getList(), annotatedElementKey, evaluationContext);
        if (listObject instanceof Collection) {
            Collection<?> list = (Collection<?>) listObject;
            for (Object item : list) {
                StandardEvaluationContext itemContext = new StandardEvaluationContext(item);
                itemContext.setVariables(evaluationContext.getVariables());
                Map<String, String> expressionValues = processTemplate(getSpElTemplates(logRecordOps), methodExecuteResult, beforeFunctionNameAndReturnMap);
                logRecords.add(buildLogRecord(logRecordOps, expressionValues, methodExecuteResult.getMethod()));
            }
        }
        return logRecords;
    }

    private List<String> getSpElTemplates(LogRecordOps logRecordOps) {
        List<String> spElTemplates = new ArrayList<>();
        spElTemplates.add(logRecordOps.getType());
        spElTemplates.add(logRecordOps.getBizNo());
        spElTemplates.add(logRecordOps.getSubType());
        spElTemplates.add(logRecordOps.getExtra());
        spElTemplates.add(logRecordOps.getList());
        spElTemplates.add(logRecordOps.getSuccessLogTemplate());
        spElTemplates.add(logRecordOps.getFailLogTemplate());
        return spElTemplates;
    }

    private LogRecord buildLogRecord(LogRecordOps logRecordOps, Map<String, String> expressionValues, Method method) {
        return LogRecord.builder()
                .tenant(expressionValues.get("tenant"))
                .type(expressionValues.get(logRecordOps.getType()))
                .bizNo(expressionValues.get(logRecordOps.getBizNo()))
                .operator(expressionValues.get(logRecordOps.getOperatorId()))
                .subType(expressionValues.get(logRecordOps.getSubType()))
                .extra(expressionValues.get(logRecordOps.getExtra()))
                .codeVariable(getCodeVariable(method))
                .action(expressionValues.get(logRecordOps.getSuccessLogTemplate()))
                .fail(false)
                .createTime(new Date())
                .list(expressionValues.get(logRecordOps.getList()))
                .build();
    }

    private Map<CodeVariableType, Object> getCodeVariable(Method method) {
        Map<CodeVariableType, Object> map = new HashMap<>();
        map.put(CodeVariableType.ClassName, method.getDeclaringClass());
        map.put(CodeVariableType.MethodName, method.getName());
        return map;
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
