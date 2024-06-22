package com.mzt.logapi.context;

import org.springframework.util.CollectionUtils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author muzhantong
 * create on 2021/2/9 2:22 下午
 */
public class LogRecordContext {

    private static final InheritableThreadLocal<Deque<Map<String, Object>>> VARIABLE_MAP_STACK = new InheritableThreadLocal<>();

    private static final InheritableThreadLocal<Map<String, Object>> GLOBAL_VARIABLE_MAP = new InheritableThreadLocal<>();

    private LogRecordContext() {
        throw new IllegalStateException("Utility class");
    }

    public static void putVariable(String name, Object value) {
        if (VARIABLE_MAP_STACK.get() == null) {
            Deque<Map<String, Object>> stack = new ArrayDeque<>();
            VARIABLE_MAP_STACK.set(stack);
        }
        Deque<Map<String, Object>> mapStack = VARIABLE_MAP_STACK.get();
        if (mapStack.isEmpty()) {
            VARIABLE_MAP_STACK.get().push(new HashMap<>());
        }
        VARIABLE_MAP_STACK.get().element().put(name, value);
    }

    public static void putGlobalVariable(String name, Object value) {
        if (GLOBAL_VARIABLE_MAP.get() == null) {
            GLOBAL_VARIABLE_MAP.set(new HashMap<>());
        }
        GLOBAL_VARIABLE_MAP.get().put(name, value);
    }

    public static Object getVariable(String key) {
        Map<String, Object> variableMap = VARIABLE_MAP_STACK.get().peek();
        return variableMap == null ? null : variableMap.get(key);
    }

    public static Object getMethodOrGlobal(String key) {
        Object result = null;
        Map<String, Object> variableMap = VARIABLE_MAP_STACK.get().peek();
        if (!CollectionUtils.isEmpty(variableMap) && (result = variableMap.get(key)) != null) {
            return result;
        }
        Map<String, Object> globalMap = GLOBAL_VARIABLE_MAP.get();
        if (!CollectionUtils.isEmpty(globalMap)) {
            return globalMap.get(key);
        }
        return result;
    }

    public static Map<String, Object> getVariables() {
        Deque<Map<String, Object>> mapStack = VARIABLE_MAP_STACK.get();
        return mapStack == null ? new HashMap<>() : mapStack.peek();
    }

    public static Map<String, Object> getGlobalVariableMap() {
        return GLOBAL_VARIABLE_MAP.get();
    }

    public static void clear() {
        if (VARIABLE_MAP_STACK.get() != null) {
            VARIABLE_MAP_STACK.get().pop();
        }
    }

    public static void clearGlobal() {
        if (GLOBAL_VARIABLE_MAP.get() != null) {
            GLOBAL_VARIABLE_MAP.get().clear();
        }
    }

    /**
     * 日志使用方不需要使用到这个方法
     * 每进入一个方法初始化一个 span 放入到 stack中，方法执行完后 pop 掉这个span
     */
    public static void putEmptySpan() {
        Deque<Map<String, Object>> mapStack = VARIABLE_MAP_STACK.get();
        if (mapStack == null) {
            Deque<Map<String, Object>> stack = new ArrayDeque<>();
            VARIABLE_MAP_STACK.set(stack);
        }
        VARIABLE_MAP_STACK.get().push(new HashMap<>());

        if (GLOBAL_VARIABLE_MAP.get() == null) {
            GLOBAL_VARIABLE_MAP.set(new HashMap<>());
        }
    }
}
