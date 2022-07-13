package com.mzt.logapi.starter.annotation;

import java.lang.annotation.*;

/**
 * @author muzhantong
 * create on 2020/4/29 3:22 下午
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface LogRecord {
    /**
     * 方法执行成功后的日志模版
     */
    String success();

    /**
     * 方法执行失败后的日志模版
     */
    String fail() default "";

    /**
     * 日志的操作人
     */
    String operator() default "";

    /**
     * 操作日志的类型，比如：订单类型、商品类型
     */
    String type();

    /**
     * 日志的子类型，比如订单的C端日志，和订单的B端日志，type都是订单类型，但是子类型不一样
     */
    String subType() default "";

    /**
     * 日志绑定的业务标识
     */
    String bizNo();

    /**
     * 日志的额外信息
     */
    String extra() default "";

    String condition() default "";

    /**
     * <p>是否是批量操作
     * */
    boolean isBatch() default false;

    /**
     * 操作类型，比如：增、删、改，可自定义枚举，便于查询某种操作类型的日志
     */
    String actionType() default "";
}
