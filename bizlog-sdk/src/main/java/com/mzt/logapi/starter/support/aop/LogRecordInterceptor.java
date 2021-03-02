package com.mzt.logapi.starter.support.aop;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.starter.support.parse.LogRecordValueParser;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
        boolean success = true;
        String errorMsg = "";
        Throwable throwable = null;
        try {
            LogRecordContext.clear();
            ret = invoker.proceed();
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throwable = e;
        }
        try {
            Collection<LogRecordOps> operations = getLogRecordOperationSource().computeLogRecordOperations(method, targetClass);
            if (!CollectionUtils.isEmpty(operations)) {
                recordExecute(ret, method, args, operations, targetClass, success, errorMsg);
            }
        } catch (Exception t) {
            //记录日志错误不要影响业务
            log.error("log record parse exception", t);
        } finally {
            LogRecordContext.clear();
        }
        if (throwable != null) {
            throw throwable;
        }
        return ret;
    }

    private void recordExecute(Object ret, Method method, Object[] args, Collection<LogRecordOps> operations,
                               Class<?> targetClass, boolean success, String errorMsg) {
        for (LogRecordOps operation : operations) {
            try {
                String action = "";
                if (success) {
                    action = operation.getSuccessLogTemplate();
                } else {
                    action = operation.getFailLogTemplate();
                    if (StringUtils.isEmpty(action)) {
                        //执行失败，并且没有配失败日志模版则忽略
                        continue;
                    }
                }
                String bizKey = operation.getBizKey();
                String bizNo = operation.getBizNo();
                String operatorId = operation.getOperatorId();
                String category = operation.getCategory();
                String detail = operation.getDetail();
                //获取需要解析的表达式
                List<String> spElTemplates;
                String realOperator = "";
                if (StringUtils.isEmpty(operatorId)) {
                    spElTemplates = Lists.newArrayList(bizKey, bizNo, action, detail);
                    if (operatorGetService.getUser() == null || StringUtils.isEmpty(operatorGetService.getUser().getOperatorId())) {
                        throw new IllegalArgumentException("user is null");
                    }
                    realOperator = operatorGetService.getUser().getOperatorId();
                } else {
                    spElTemplates = Lists.newArrayList(bizKey, bizNo, action, operatorId, detail);
                }
                Map<String, String> expressionValues = processTemplate(spElTemplates, ret, targetClass, method, args, errorMsg);

                LogRecord logRecord = LogRecord.builder()
                        .tenant(tenantId)
                        .bizKey(expressionValues.get(bizKey))
                        .bizNo(expressionValues.get(bizNo))
                        .operator(!StringUtils.isEmpty(realOperator) ? realOperator : expressionValues.get(operatorId))
                        .category(category)
                        .detail(expressionValues.get(detail))
                        .action(expressionValues.get(action))
                        .createTime(new Date())
                        .build();

                //save log 需要新开事务，失败日志不能因为事务回滚而丢失
                Preconditions.checkNotNull(bizLogService, "bizLogService not init!!");
                bizLogService.record(logRecord);
            } catch (Exception t) {
                log.error("log record execute exception", t);
            }
        }
    }

    private Class<?> getTargetClass(Object target) {
        return AopProxyUtils.ultimateTargetClass(target);
    }

    public LogRecordOperationSource getLogRecordOperationSource() {
        return logRecordOperationSource;
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
}
