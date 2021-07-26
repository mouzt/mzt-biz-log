package com.mzt.logapi.starter.support.aop;

import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * DATE 4:45 PM
 *
 * @author mzt.
 */
public class LogRecordPointcut extends StaticMethodMatcherPointcut implements Serializable {


    private LogRecordOperationSource logRecordOperationSource;

    @Override
    public boolean matches(@NonNull Method method, @NonNull Class<?> targetClass) {
        return !CollectionUtils.isEmpty(logRecordOperationSource.computeLogRecordOperations(method, targetClass));
    }

    void setLogRecordOperationSource(LogRecordOperationSource logRecordOperationSource) {
        this.logRecordOperationSource = logRecordOperationSource;
    }
}