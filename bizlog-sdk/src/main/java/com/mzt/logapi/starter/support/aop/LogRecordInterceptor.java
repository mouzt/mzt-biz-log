package com.mzt.logapi.starter.support.aop;

import com.mzt.logapi.beans.CodeVariableType;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.beans.MethodExecuteResult;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.IFunctionService;
import com.mzt.logapi.service.ILogRecordPerformanceMonitor;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.support.parse.LogFunctionParser;
import com.mzt.logapi.starter.support.parse.LogRecordValueParser;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

import static com.mzt.logapi.service.ILogRecordPerformanceMonitor.*;

/**
 * DATE 5:39 PM
 *
 * @author mzt.
 */
@Slf4j
public class LogRecordInterceptor extends LogRecordValueParser implements MethodInterceptor, Serializable, SmartInitializingSingleton, BeanFactoryAware {

    private LogRecordOperationSource logRecordOperationSource;

    private String tenantId;

    private ILogRecordService bizLogService;

    private IOperatorGetService operatorGetService;

    private ILogRecordPerformanceMonitor logRecordPerformanceMonitor;

    private boolean joinTransaction;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        return execute(invocation, invocation.getThis(), method, invocation.getArguments());
    }

    private Object execute(MethodInvocation invoker, Object target, Method method, Object[] args) throws Throwable {
        //代理不拦截
        if (AopUtils.isAopProxy(target)) {
            return invoker.proceed();
        }
        StopWatch stopWatch = new StopWatch(MONITOR_NAME);
        stopWatch.start(MONITOR_TASK_BEFORE_EXECUTE);
        Class<?> targetClass = getTargetClass(target);
        Object ret = null;
        MethodExecuteResult methodExecuteResult = new MethodExecuteResult(method, args, targetClass);
        LogRecordContext.putEmptySpan();
        Collection<LogRecordOps> operations = new ArrayList<>();
        Map<String, String> functionNameAndReturnMap = new HashMap<>();
        try {
            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
            List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(spElTemplates, targetClass, method, args);
        } catch (Exception e) {
            log.error("log record parse before function exception", e);
        } finally {
            stopWatch.stop();
        }

        try {
            ret = invoker.proceed();
            methodExecuteResult.setResult(ret);
            methodExecuteResult.setSuccess(true);
        } catch (Exception e) {
            methodExecuteResult.setSuccess(false);
            methodExecuteResult.setThrowable(e);
            methodExecuteResult.setErrorMsg(e.getMessage());
        }
        stopWatch.start(MONITOR_TASK_AFTER_EXECUTE);
        try {
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(methodExecuteResult, functionNameAndReturnMap, operations);
            }
        } catch (Exception t) {
            log.error("log record parse exception", t);
            throw t;
        } finally {
            LogRecordContext.clear();
            stopWatch.stop();
            try {
                logRecordPerformanceMonitor.print(stopWatch);
            } catch (Exception e) {
                log.error("execute exception", e);
            }
        }

        if (methodExecuteResult.getThrowable() != null) {
            throw methodExecuteResult.getThrowable();
        }
        return ret;
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

    private void recordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                               Collection<LogRecordOps> operations) {
        for (LogRecordOps operation : operations) {
            try {
                if (StringUtils.isEmpty(operation.getSuccessLogTemplate())
                        && StringUtils.isEmpty(operation.getFailLogTemplate())) {
                    continue;
                }
                if (exitsCondition(methodExecuteResult, functionNameAndReturnMap, operation)) continue;
                
                // 处理批量记录日志
                if (!StringUtils.isEmpty(operation.getList())) {
                    processBatchLogRecord(methodExecuteResult, functionNameAndReturnMap, operation);
                } else {
                    // 处理单条记录日志
                    if (methodExecuteResult.isSuccess()) {
                        successRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                    } else {
                        failRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                    }
                }
            } catch (Exception e) {
                log.error("log record execute exception", e);
                if (joinTransaction) throw e;
            }
        }
    }

    private void processBatchLogRecord(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                      LogRecordOps operation) {
        try {
            // 获取列表变量名
            String listVarName = operation.getList();
            // 解析列表变量，获取实际的列表对象
            String listExpression = listVarName.replaceAll("[{}]", "");
            
            // 使用 singleProcessTemplate 方法解析列表表达式
            String listValue = singleProcessTemplate(methodExecuteResult, listExpression, functionNameAndReturnMap);
            Object listObj = LogRecordContext.getVariable(listExpression.replace("#", ""));
            
            if (listObj instanceof List) {
                List<?> dataList = (List<?>) listObj;
                List<LogRecord> logRecords = new ArrayList<>();
                
                for (Object item : dataList) {
                    // 将当前列表项放入上下文
                    LogRecordContext.putVariable(listExpression.replace("#", ""), item);
                    
                    // 处理成功或失败的日志记录
                    if (methodExecuteResult.isSuccess()) {
                        String action = operation.getSuccessLogTemplate();
                        if (!StringUtils.isEmpty(action)) {
                            List<String> spElTemplates = getSpElTemplates(operation, action);
                            String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
                            Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
                            
                            LogRecord logRecord = createLogRecord(methodExecuteResult.getMethod(), false, operation, 
                                    operatorIdFromService, action, expressionValues);
                            if (logRecord != null) {
                                logRecords.add(logRecord);
                            }
                        }
                    } else {
                        String action = operation.getFailLogTemplate();
                        if (!StringUtils.isEmpty(action)) {
                            List<String> spElTemplates = getSpElTemplates(operation, action);
                            String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
                            Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
                            
                            LogRecord logRecord = createLogRecord(methodExecuteResult.getMethod(), true, operation, 
                                    operatorIdFromService, action, expressionValues);
                            if (logRecord != null) {
                                logRecords.add(logRecord);
                            }
                        }
                    }
                    
                    // 从上下文中移除当前列表项
                    LogRecordContext.putVariable(listExpression.replace("#", ""), null);
                }
                
                // 批量保存日志
                if (!logRecords.isEmpty()) {
                    bizLogService.recordList(logRecords);
                }
            } else {
                log.error("List variable is not a List type: {}", listVarName);
            }
        } catch (Exception e) {
            log.error("Process batch log record error", e);
        }
    }

    private void successRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                     LogRecordOps operation) {
        if (StringUtils.isEmpty(operation.getSuccessLogTemplate())) return;
        String action = operation.getSuccessLogTemplate();
        boolean flag = false;
        if (!StringUtils.isEmpty(operation.getIsSuccess())) {
            String isSuccess = singleProcessTemplate(methodExecuteResult, operation.getIsSuccess(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(isSuccess, "false")) {
                action = operation.getFailLogTemplate();
                flag = true;
            }
        }
        if (StringUtils.isEmpty(action)) {
            return;
        }
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), flag, operation, operatorIdFromService, action, expressionValues);
    }

    private void failRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                   LogRecordOps operation) {
        if (StringUtils.isEmpty(operation.getFailLogTemplate())) return;

        String action = operation.getFailLogTemplate();
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);

        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), true, operation, operatorIdFromService, action, expressionValues);
    }

    private boolean exitsCondition(MethodExecuteResult methodExecuteResult,
                                  Map<String, String> functionNameAndReturnMap, LogRecordOps operation) {
        if (!StringUtils.isEmpty(operation.getCondition())) {
            String condition = singleProcessTemplate(methodExecuteResult, operation.getCondition(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(condition, "false")) return true;
        }
        return false;
    }

    private LogRecord createLogRecord(Method method, boolean flag, LogRecordOps operation, String operatorIdFromService,
                         String action, Map<String, String> expressionValues) {
        if (StringUtils.isEmpty(expressionValues.get(action)) ||
                (!diffSameWhetherSaveLog && action.contains("#") && Objects.equals(action, expressionValues.get(action)))) {
            return null;
        }
        return LogRecord.builder()
                .tenant(tenantId)
                .type(expressionValues.get(operation.getType()))
                .bizNo(expressionValues.get(operation.getBizNo()))
                .operator(getRealOperatorId(operation, operatorIdFromService, expressionValues))
                .subType(expressionValues.get(operation.getSubType()))
                .extra(expressionValues.get(operation.getExtra()))
                .codeVariable(getCodeVariable(method))
                .action(expressionValues.get(action))
                .fail(flag)
                .createTime(new Date())
                .build();
    }

    private void saveLog(Method method, boolean flag, LogRecordOps operation, String operatorIdFromService,
                         String action, Map<String, String> expressionValues) {
        LogRecord logRecord = createLogRecord(method, flag, operation, operatorIdFromService, action, expressionValues);
        if (logRecord != null) {
            bizLogService.record(logRecord);
        }
    }

    private Map<CodeVariableType, Object> getCodeVariable(Method method) {
        Map<CodeVariableType, Object> map = new HashMap<>();
        map.put(CodeVariableType.ClassName, method.getDeclaringClass());
        map.put(CodeVariableType.MethodName, method.getName());
        return map;
    }

    private List<String> getSpElTemplates(LogRecordOps operation, String... actions) {
        List<String> spElTemplates = new ArrayList<>();
        spElTemplates.add(operation.getType());
        spElTemplates.add(operation.getBizNo());
        spElTemplates.add(operation.getSubType());
        spElTemplates.add(operation.getExtra());
        spElTemplates.addAll(Arrays.asList(actions));
        return spElTemplates;
    }

    private String getRealOperatorId(LogRecordOps operation, String operatorIdFromService,
                                    Map<String, String> expressionValues) {
        return !StringUtils.isEmpty(operatorIdFromService) ? operatorIdFromService : expressionValues.get(operation.getOperatorId());
    }

    private String getOperatorIdFromServiceAndPutTemplate(LogRecordOps operation, List<String> spElTemplates) {

        String realOperatorId = "";
        if (StringUtils.isEmpty(operation.getOperatorId())) {
            realOperatorId = operatorGetService.getUser().getOperatorId();
            if (StringUtils.isEmpty(realOperatorId)) {
                throw new IllegalArgumentException("[LogRecord] operator is null");
            }
        } else {
            spElTemplates.add(operation.getOperatorId());
        }
        return realOperatorId;
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }


    public void setLogRecordOperationSource(LogRecordOperationSource logRecordOperationSource) {
        this.logRecordOperationSource = logRecordOperationSource;
    }

    public void setTenant(String tenant) {
        this.tenantId = tenant;
    }

    public void setLogRecordService(ILogRecordService bizLogService) {
        this.bizLogService = bizLogService;
    }

    public void setLogRecordPerformanceMonitor(ILogRecordPerformanceMonitor logRecordPerformanceMonitor) {
        this.logRecordPerformanceMonitor = logRecordPerformanceMonitor;
    }

    public void setJoinTransaction(boolean joinTransaction) {
        this.joinTransaction = joinTransaction;
    }

    public void setDiffSameWhetherSaveLog(boolean diffLog) {
        this.diffSameWhetherSaveLog = diffLog;
    }

    @Override
    public void afterSingletonsInstantiated() {
        bizLogService = beanFactory.getBean(ILogRecordService.class);
        operatorGetService = beanFactory.getBean(IOperatorGetService.class);
        this.setLogFunctionParser(new LogFunctionParser(beanFactory.getBean(IFunctionService.class)));
        this.setDiffParseFunction(beanFactory.getBean(DiffParseFunction.class));
    }
}
