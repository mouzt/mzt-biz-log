package com.mzt.logapi.starter.annotation;

import java.lang.annotation.*;

/**
 *  比较对象中的唯一标识
 * @author focus
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface CompareID {
}
