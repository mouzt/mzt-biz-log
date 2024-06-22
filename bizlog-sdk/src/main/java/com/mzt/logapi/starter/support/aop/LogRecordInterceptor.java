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
public class LogRecordInterceptor extends LogRecordValueParser implements MethodInterceptor, Serializable, SmartInitializingSingleton {

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
                if (!methodExecuteResult.isSuccess()) {
                    failRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                } else {
                    successRecordExecute(methodExecuteResult, functionNameAndReturnMap, operation);
                }
            } catch (Exception t) {
                log.error("log record execute exception", t);
                if (joinTransaction) throw t;
            }
        }
    }

    private void successRecordExecute(MethodExecuteResult methodExecuteResult, Map<String, String> functionNameAndReturnMap,
                                      LogRecordOps operation) {
        // 若存在 isSuccess 条件模版，解析出成功/失败的模版
        String action = "";
        boolean flag = true;
        if (!StringUtils.isEmpty(operation.getIsSuccess())) {
            String condition = singleProcessTemplate(methodExecuteResult, operation.getIsSuccess(), functionNameAndReturnMap);
            if (StringUtils.endsWithIgnoreCase(condition, "true")) {
                action = operation.getSuccessLogTemplate();
            } else {
                action = operation.getFailLogTemplate();
                flag = false;
            }
        } else {
            action = operation.getSuccessLogTemplate();
        }
        if (StringUtils.isEmpty(action)) {
            // 没有日志内容则忽略
            return;
        }
        List<String> spElTemplates = getSpElTemplates(operation, action);
        String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);
        Map<String, String> expressionValues = processTemplate(spElTemplates, methodExecuteResult, functionNameAndReturnMap);
        saveLog(methodExecuteResult.getMethod(), !flag, operation, operatorIdFromService, action, expressionValues);
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

    private void saveLog(Method method, boolean flag, LogRecordOps operation, String operatorIdFromService,
                         String action, Map<String, String> expressionValues) {
        if (StringUtils.isEmpty(expressionValues.get(action)) ||
                (!diffSameWhetherSaveLog && action.contains("#") && Objects.equals(action, expressionValues.get(action)))) {
            return;
        }
        LogRecord logRecord = LogRecord.builder()
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

        bizLogService.record(logRecord);
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

    private String getRealOperatorId(LogRecordOps operation, String operatorIdFromService, Map<String, String> expressionValues) {
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
