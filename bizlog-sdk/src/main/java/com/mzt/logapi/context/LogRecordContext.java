package com.mzt.logapi.context;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author muzhantong
 * create on 2021/2/9 2:22 下午
 */
public class LogRecordContext {

    private static final InheritableThreadLocal<Map<String, Object>> variableMap = new InheritableThreadLocal<>();


    public static void putVariable(String name, Object value) {
        if (variableMap.get() == null) {
            HashMap<String, Object> map = Maps.newHashMap();
            map.put(name, value);
            variableMap.set(map);
        }
        variableMap.get().put(name, value);
    }

    public static Map<String, Object> getVariables() {
        return variableMap.get() == null ? Maps.newHashMap() : variableMap.get();
    }

    public static void clear() {
        if (variableMap.get() != null) {
            variableMap.get().clear();
        }
    }
}
