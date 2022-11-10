package com.mzt.logapi.starter.annotation;

import java.lang.annotation.*;

/**
 * @author wulang
 **/
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecords {
    LogRecord[] value();
}
