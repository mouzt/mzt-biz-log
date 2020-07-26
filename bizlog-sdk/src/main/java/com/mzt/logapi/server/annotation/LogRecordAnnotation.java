package com.mzt.logapi.server.annotation;

import java.lang.annotation.*;

/**
 * @author muzhantong
 * create on 2020/4/29 3:22 下午
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecordAnnotation {
    String success();
    String fail() default "";
    String operator() default "";;
    String operatorId() default "";;
    String bizKey();
    String bizNo() default "";
    String category() default "";
}
