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
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
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
        List<Map<String, String>> functionNameAndReturnMap = new ArrayList<>();
        try {
            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(operations,targetClass, method, args);
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



    private void recordExecute(MethodExecuteResult methodExecuteResult, List<Map<String, String>> functionNameAndReturnMap,
                               Collection<LogRecordOps> operations) {
        for (LogRecordOps operation : operations) {
            try {
                if (StringUtils.isEmpty(operation.getSuccessLogTemplate())
                        && StringUtils.isEmpty(operation.getFailLogTemplate())) {
                    continue;
                }
                // 处理批量记录日志
                if (!StringUtils.isEmpty(operation.getList())) {
                    processBatchLogRecord(methodExecuteResult, functionNameAndReturnMap, operation);
                    return;
                }
                //if (exitsCondition(methodExecuteResult, functionNameAndReturnMap, operation)) continue;
                // 处理单条记录日志
                if (methodExecuteResult.isSuccess()) {
                    successRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                } else {
                    failRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
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
            String spelListExpression = listVarName.replaceAll("\\{", "").replaceAll("}", "");
            // 使用 singleProcessTemplate 方法解析列表表达式
            Object listObj = super.parseValueBySpel(spelListExpression, methodExecuteResult);

            if (listObj instanceof List) {
                List<?> dataList = (List<?>) listObj;
                List<LogRecord> logRecords = new ArrayList<>();

                for (Object item : dataList) {
                    // 将当前列表项放入上下文
                    LogRecordContext.putVariable(LIST_ITEM, item);
                    String action = methodExecuteResult.isSuccess() ? operation.getSuccessLogTemplate() : operation.getFailLogTemplate();
                    List<String> spElTemplates = getSpElTemplates(operation, action);
                    String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
                    Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
                    if(!StringUtils.isEmpty(operation.getCondition()) && StringUtils.endsWithIgnoreCase(expressionValues.get(operation.getCondition()), "false")) {
                        // 如果条件不满足，则跳过当前列表项
                        continue;
                    }
                    if (!StringUtils.isEmpty(action)) {
                        LogRecord logRecord = createLogRecord(methodExecuteResult.getMethod(), methodExecuteResult.isSuccess(), operation,
                                operatorIdFromService, action, expressionValues);
                        if (logRecord != null) {
                            logRecords.add(logRecord);
                        }
                    }
                    // 从上下文中移除当前列表项
                    LogRecordContext.putVariable(LIST_ITEM, null);
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
        boolean success = true;
        if (!StringUtils.isEmpty(operation.getIsSuccess())) {
            String isSuccess = singleProcessTemplate(methodExecuteResult, operation.getIsSuccess(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(isSuccess, "false")) {
                action = operation.getFailLogTemplate();
                success = false;
            }
        }
        if (StringUtils.isEmpty(action)) {
            return;
        }
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), success, operation, operatorIdFromService, action, expressionValues);
    }

    private void failRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                   LogRecordOps operation) {
        if (StringUtils.isEmpty(operation.getFailLogTemplate())) return;

        String action = operation.getFailLogTemplate();
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);

        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), false, operation, operatorIdFromService, action, expressionValues);
    }

    private boolean exitsCondition(MethodExecuteResult methodExecuteResult,
                                   Map<String, String> functionNameAndReturnMap, LogRecordOps operation) {
        if (!StringUtils.isEmpty(operation.getCondition())) {
            String condition = singleProcessTemplate(methodExecuteResult, operation.getCondition(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(condition, "false")) return true;
        }
        return false;
    }

    private LogRecord createLogRecord(Method method, boolean success, LogRecordOps operation, String operatorIdFromService,
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
                .fail(!success)
                .createTime(new Date())
                .build();
    }

    private void saveLog(Method method, boolean success, LogRecordOps operation, String operatorIdFromService,
                         String action, Map<String, String> expressionValues) {
        LogRecord logRecord = createLogRecord(method, success, operation, operatorIdFromService, action, expressionValues);
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
