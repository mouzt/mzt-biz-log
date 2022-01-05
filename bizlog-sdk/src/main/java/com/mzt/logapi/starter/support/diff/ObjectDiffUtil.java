package com.mzt.logapi.starter.support.diff;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author muzhantong
 * create on 2022/1/3 8:20 下午
 */
@Slf4j
public class ObjectDiffUtil {

    private static IDiffItemsToLogContentService diffItemsToLogContentService;


    public void setDiffItemsToLogContentService(IDiffItemsToLogContentService IDiffItemsToLogContentService) {
        ObjectDiffUtil.diffItemsToLogContentService = IDiffItemsToLogContentService;
    }

    public static String diff(Object source, Object target) {
        if (source == null && target == null) {
            return "";
        }
        if (source == null || target == null) {
            try {
                Class<?> clazz = source == null ? target.getClass() : source.getClass();
                source = source == null ? clazz.newInstance() : source;
                target = target == null ? clazz.newInstance() : target;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Objects.equals(source.getClass(), target.getClass())) {
            log.error("diff的两个对象类型不同, source.class={}, target.class={}", source.getClass().toString(), target.getClass().toString());
            return "";
        }
        DiffNode diffNode = ObjectDifferBuilder.buildDefault().compare(target, source);
        return diffItemsToLogContentService.toLogContent(diffNode, source, target);
    }
}
