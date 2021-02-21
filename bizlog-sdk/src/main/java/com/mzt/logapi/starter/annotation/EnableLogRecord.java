package com.mzt.logapi.starter.annotation;

import com.mzt.logapi.starter.support.LogRecordConfigureSelector;
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

    String tenant();

    /**
     * Indicate how caching advice should be applied. The default is
     * {@link AdviceMode#PROXY}.
     *
     * @return 代理方式
     * @see AdviceMode
     */
    AdviceMode mode() default AdviceMode.PROXY;
}
