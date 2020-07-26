package com.mzt.logapi.server.annotation;

import com.mzt.logapi.server.support.LogRecordConfigureSelector;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * DATE 6:28 PM
 *
 * @author mzt.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LogRecordConfigureSelector.class)
public @interface EnableLogRecord {

    String bizLine();

    /**
     * Indicate how caching advice should be applied. The default is
     * {@link AdviceMode#PROXY}.
     * @see AdviceMode
     */
    AdviceMode mode() default AdviceMode.PROXY;
}
