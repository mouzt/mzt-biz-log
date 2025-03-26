package com.mzt.logapi.service.impl;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.diff.IDiffItemsToLogContentService;
import com.mzt.logapi.util.diff.ArrayDiffer;
import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.comparison.ComparisonService;
import de.danielbechler.diff.node.DiffNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author muzhantong
 * create on 2022/2/8 3:44 下午
 */
@Slf4j
public class DiffParseFunction {
    public static final String diffFunctionName = "_DIFF";
    public static final String OLD_OBJECT = "_oldObj";

    private IDiffItemsToLogContentService diffItemsToLogContentService;

    private final Set<Class<?>> comparisonSet = new HashSet<>();

    //@Override
    public String functionName() {
        return diffFunctionName;
    }

    //@Override
    public String diff(Object source, Object target) {
        if (source == null && target == null) {
            return "";
        }
        if (source == null || target == null) {
            try {
                Class<?> clazz = source == null ? target.getClass() : source.getClass();
                source = source == null ? clazz.getDeclaredConstructor().newInstance() : source;
                target = target == null ? clazz.getDeclaredConstructor().newInstance() : target;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Objects.equals(AopUtils.getTargetClass(source), AopUtils.getTargetClass(target))) {
            log.error("diff的两个对象类型不同, source.class={}, target.class={}", source.getClass().toString(), target.getClass().toString());
            return "";
        }
        ObjectDifferBuilder objectDifferBuilder = ObjectDifferBuilder.startBuilding();
        ObjectDifferBuilder register = objectDifferBuilder
                .differs().register((differDispatcher, nodeQueryService) ->
                        new ArrayDiffer(differDispatcher, (ComparisonService) objectDifferBuilder.comparison(), objectDifferBuilder.identity()));
        for (Class<?> clazz : comparisonSet) {
            register.comparison().ofType(clazz).toUseEqualsMethod();
        }
        DiffNode diffNode = register.build().compare(target, source);
        return diffItemsToLogContentService.toLogContent(diffNode, source, target);
    }

    public String diff(Object newObj) {
        Object oldObj = LogRecordContext.getMethodOrGlobal(OLD_OBJECT);
        return diff(oldObj, newObj);
    }

    public void setDiffItemsToLogContentService(IDiffItemsToLogContentService diffItemsToLogContentService) {
        this.diffItemsToLogContentService = diffItemsToLogContentService;
    }

    public void addUseEqualsClass(List<String> classList) {
        if (classList != null && !classList.isEmpty()) {
            for (String clazz : classList) {
                try {
                    Class<?> aClass = Class.forName(clazz);
                    comparisonSet.add(aClass);
                } catch (ClassNotFoundException e) {
                    log.warn("无效的比对类型, className={}", clazz);
                }
            }
        }
    }

    public void addUseEqualsClass(Class clazz) {
        comparisonSet.add(clazz);
    }
}
