package com.mzt.logapi.starter.support.aop;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.starter.support.parse.LogRecordValueParser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * DATE 5:39 PM
 *
 * @author mzt.
 */
@Slf4j
public class LogRecordInterceptor extends LogRecordValueParser implements InitializingBean, MethodInterceptor, Serializable {

    private LogRecordOperationSource logRecordOperationSource;

    private String tenantId;

    private ILogRecordService bizLogService;

    private IOperatorGetService operatorGetService;

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        return execute(invocation, invocation.getThis(), method, invocation.getArguments());
    }

    private Object execute(MethodInvocation invoker, Object target, Method method, Object[] args) throws Throwable {
        Class<?> targetClass = getTargetClass(target);
        Object ret = null;
        MethodExecuteResult methodExecuteResult = new MethodExecuteResult(true, null, "");
        LogRecordContext.putEmptySpan();
        Collection<LogRecordOps> operations = new ArrayList<>();
        Map<String, String> functionNameAndReturnMap = new HashMap<>();
        try {
            operations = logRecordOperationSource.computeLogRecordOperations(method, targetClass);
            List<String> spElTemplates = getBeforeExecuteFunctionTemplate(operations);
            functionNameAndReturnMap = processBeforeExecuteFunctionTemplate(spElTemplates, targetClass, method, args);
        } catch (Exception e) {
            log.error("log record parse before function exception", e);
        }
        try {
            ret = invoker.proceed();
        } catch (Exception e) {
            methodExecuteResult = new MethodExecuteResult(false, e, e.getMessage());
        }
        try {
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(ret, method, args, operations, targetClass,
                        methodExecuteResult.isSuccess(), methodExecuteResult.getErrorMsg(), functionNameAndReturnMap);
            }
        } catch (Exception t) {
            //记录日志错误不要影响业务
            log.error("log record parse exception", t);
        } finally {
            LogRecordContext.clear();
        }
        if (methodExecuteResult.throwable != null) {
            throw methodExecuteResult.throwable;
        }
        return ret;
    }

    private List<String> getBeforeExecuteFunctionTemplate(Collection<LogRecordOps> operations) {
        List<String> spElTemplates = new ArrayList<>();
        for (LogRecordOps operation : operations) {
            //执行之前的函数，失败模版不解析
            List<String> templates = getSpElTemplates(operation, operation.getSuccessLogTemplate());
            if(!CollectionUtils.isEmpty(templates)){
                spElTemplates.addAll(templates);
            }
        }
        return spElTemplates;
    }

    private void recordExecute(Object ret, Method method, Object[] args, Collection<LogRecordOps> operations,
                               Class<?> targetClass, boolean success, String errorMsg, Map<String, String> functionNameAndReturnMap) {
        for (LogRecordOps operation : operations) {
            try {
                String action = getActionContent(success, operation);
                if (StringUtils.isEmpty(action)) {
                    //没有日志内容则忽略
                    continue;
                }
                //获取需要解析的表达式
                List<String> spElTemplates = getSpElTemplates(operation, action);
                String operatorIdFromService = getOperatorIdFromServiceAndPutTemplate(operation, spElTemplates);

                Map<String, String> expressionValues = processTemplate(spElTemplates, ret, targetClass, method, args, errorMsg, functionNameAndReturnMap);
                if (logConditionPassed(operation.getCondition(), expressionValues)) {
                    LogRecord logRecord = LogRecord.builder()
                            .tenant(tenantId)
                            .bizKey(expressionValues.get(operation.getBizKey()))
                            .bizNo(expressionValues.get(operation.getBizNo()))
                            .operator(getRealOperatorId(operation, operatorIdFromService, expressionValues))
                            .category(operation.getCategory())
                            .detail(expressionValues.get(operation.getDetail()))
                            .action(expressionValues.get(action))
                            .createTime(new Date())
                            .build();

                    //如果 action 为空，不记录日志
                    if (StringUtils.isEmpty(logRecord.getAction())) {
                        continue;
                    }
                    //save log 需要新开事务，失败日志不能因为事务回滚而丢失
                    Preconditions.checkNotNull(bizLogService, "bizLogService not init!!");
                    bizLogService.record(logRecord);
                }
            } catch (Exception t) {
                log.error("log record execute exception", t);
            }
        }
    }

    private List<String> getSpElTemplates(LogRecordOps operation, String action) {
         List<String> spElTemplates = Lists.newArrayList(operation.getBizKey(), operation.getBizNo(), action, operation.getDetail());
        if (!StringUtils.isEmpty(operation.getCondition())) {
            spElTemplates.add(operation.getCondition());
        }
        return spElTemplates;
    }

    private boolean logConditionPassed(String condition, Map<String, String> expressionValues) {
        return StringUtils.isEmpty(condition) || StringUtils.endsWithIgnoreCase(expressionValues.get(condition), "true");
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

    private String getActionContent(boolean success, LogRecordOps operation) {
        String action = "";
        if (success) {
            action = operation.getSuccessLogTemplate();
        } else {
            action = operation.getFailLogTemplate();
        }
        return action;
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

    @Override
    public void afterPropertiesSet() throws Exception {
        bizLogService = beanFactory.getBean(ILogRecordService.class);
        operatorGetService = beanFactory.getBean(IOperatorGetService.class);
        Preconditions.checkNotNull(bizLogService, "bizLogService not null");
    }

    public void setOperatorGetService(IOperatorGetService operatorGetService) {
        this.operatorGetService = operatorGetService;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    static class MethodExecuteResult {
        private boolean success;
        private Throwable throwable;
        private String errorMsg;
    }
}
