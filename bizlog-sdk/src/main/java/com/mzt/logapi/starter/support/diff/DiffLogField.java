package com.mzt.logapi.starter.support.diff;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface DiffLogField {

    String name();

    String function() default "";

    //   String dateFormat() default "";
}
