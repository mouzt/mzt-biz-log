package com.mzt.logapi.util;

import org.springframework.core.GenericTypeResolver;

/**
 * @author Montos
 * @create 2021/10/27 9:24 上午
 * 泛型工具类
 */
public class GenericUtils {
    /**
     * 获取类上的泛型T
     *
     * @param o          对应实现类
     * @param interfaces 接口类型
     */
    public static Class<?> getClassT(Object o, Class<?> interfaces) {
        Class[] genericArgs = GenericTypeResolver.resolveTypeArguments(o.getClass(), interfaces);
        assert genericArgs != null;
        return genericArgs[0];
    }


}
