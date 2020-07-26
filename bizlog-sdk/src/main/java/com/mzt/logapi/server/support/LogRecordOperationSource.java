package com.mzt.logapi.server.support;

import com.mzt.logapi.beans.LogRecordOps;
import com.mzt.logapi.server.annotation.LogRecordAnnotation;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

/**
 * DATE 6:03 PM
 *
 * @author mzt.
 */
public class LogRecordOperationSource {


    public Collection<LogRecordOps> computeLogRecordOperations(Method method, Class<?> targetClass) {
        // Don't allow no-public methods as required.
        if (!Modifier.isPublic(method.getModifiers())) {
            return null;
        }

        // The method may be on an interface, but we need attributes from the target class.
        // If the target class is null, the method will be unchanged.
        Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
        // If we are dealing with method with generic parameters, find the original method.
        specificMethod = BridgeMethodResolver.findBridgedMethod(specificMethod);

        // First try is the method in the target class.
        Collection<LogRecordOps> opDef = parseLogRecordAnnotations(specificMethod);
        if (opDef != null) {
            return opDef;
        }
        return null;
    }

    private Collection<LogRecordOps> parseLogRecordAnnotations(AnnotatedElement ae) {
        Collection<LogRecordAnnotation> logRecordAnnotationAnnotations = AnnotatedElementUtils.getAllMergedAnnotations(ae, LogRecordAnnotation.class);
        Collection<LogRecordOps> ret = null;
        if (!logRecordAnnotationAnnotations.isEmpty()) {
            ret = lazyInit(ret);
            for (LogRecordAnnotation recordAnnotation : logRecordAnnotationAnnotations) {
                ret.add(parseLogRecordAnnotation(ae, recordAnnotation));
            }
        }
        return ret;
    }

    private LogRecordOps parseLogRecordAnnotation(AnnotatedElement ae, LogRecordAnnotation recordAnnotation) {
        LogRecordOps recordOps = LogRecordOps.builder()
                .successLogTemplate(recordAnnotation.success())
                .failLogTemplate(recordAnnotation.fail())
                .bizKey(recordAnnotation.bizKey())
                .bizNo(recordAnnotation.bizNo())
                .operator(recordAnnotation.operator())
                .operatorId(recordAnnotation.operatorId())
                .category(recordAnnotation.category())
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

    private Collection<LogRecordOps> lazyInit(Collection<LogRecordOps> ops) {
        return (ops != null ? ops : new ArrayList<>(1));
    }

}
