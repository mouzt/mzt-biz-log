package com.mzt.logapi.starter.support.aop;

import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logapi.starter.annotation.LogRecords;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * DATE 6:03 PM
 *
 * @author mzt.
 */
public class LogRecordOperationSource {


    public Collection<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass) {
        // Don't allow no-public methods as required.
        if (!Modifier.isPublic(method.getModifiers())) {
            return Collections.emptyList();
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // If we are dealing with method with generic parameters, find the original method.
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // First try is the method in the target class.
        Collection<LogRecordOps> logRecordOps = parseLogRecordAnnotations(specificMethod);
        Collection<LogRecordOps> logRecordsOps = parseLogRecordsAnnotations(specificMethod);
        Collection<LogRecordOps> abstractLogRecordOps = parseLogRecordAnnotations(ClassUtils.getInterfaceMethodIfPossible(method));
        Collection<LogRecordOps> abstractLogRecordsOps = parseLogRecordsAnnotations(ClassUtils.getInterfaceMethodIfPossible(method));
        HashSet<LogRecordOps> result = new HashSet<>();
        result.addAll(logRecordOps);
        result.addAll(abstractLogRecordOps);
        result.addAll(logRecordsOps);
        result.addAll(abstractLogRecordsOps);
        return result;
    }

    private Collection<LogRecordOps> parseLogRecordsAnnotations(AnnotatedElement ae) {
        Collection<LogRecordOps> res = new ArrayList<>();
        Collection<LogRecords> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecords.class);
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            logRecordAnnotationAnnotations.forEach(logRecords -> {
                LogRecord[] value = logRecords.value();
                for (LogRecord logRecord : value) {
                    res.add(parseLogRecordAnnotation(ae, logRecord));
                }
            });
        }
        return res;
    }

    private Collection<LogRecordOps> parseLogRecordAnnotations(AnnotatedElement ae) {
        Collection<LogRecord> logRecordAnnotationAnnotations = AnnotatedElementUtils.findAllMergedAnnotations(ae, LogRecord.class);
        Collection<LogRecordOps> ret = new ArrayList<>();
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            for (LogRecord recordAnnotation : logRecordAnnotationAnnotations) {
                ret.add(parseLogRecordAnnotation(ae, recordAnnotation));
            }
        }
        return ret;
    }

    private LogRecordOps parseLogRecordAnnotation(AnnotatedElement ae, LogRecord recordAnnotation) {
        LogRecordOps recordOps = LogRecordOps.builder()
                .successLogTemplate(recordAnnotation.success())
                .failLogTemplate(recordAnnotation.fail())
                .type(recordAnnotation.type())
                .bizNo(recordAnnotation.bizNo())
                .operatorId(recordAnnotation.operator())
                .subType(recordAnnotation.subType())
                .extra(recordAnnotation.extra())
                .condition(recordAnnotation.condition())
                .isSuccess(recordAnnotation.successCondition())
                .build();
        validateLogRecordOperation(ae, recordOps);
        return recordOps;
    }


    private void validateLogRecordOperation(AnnotatedElement ae, LogRecordOps recordOps) {
        if (!StringUtils.hasText(recordOps.getSuccessLogTemplate()) && !StringUtils.hasText(recordOps.getFailLogTemplate())) {
            throw new IllegalStateException("Invalid logRecord annotation configuration on '" +
                    ae.toString() + "'. 'one of successTemplate and failLogTemplate' attribute must be set.");
        }
    }

}
