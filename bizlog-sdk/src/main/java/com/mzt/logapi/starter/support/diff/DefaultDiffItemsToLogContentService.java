package com.mzt.logapi.starter.support.diff;

import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.Visit;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

/**
 * @author muzhantong
 * create on 2022/1/3 8:52 下午
 */
@Slf4j
public class DefaultDiffItemsToLogContentService implements IDiffItemsToLogContentService {

    @Override
    public String toLogContent(DiffNode diffNode, Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return "";
        }
        if (o1 == null || o2 == null) {
            try {
                Class<?> clazz = o1 == null ? o2.getClass() : o1.getClass();
                o1 = o1 == null ? clazz.newInstance() : o1;
                o2 = o2 == null ? clazz.newInstance() : o2;
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        if (!Objects.equals(o1.getClass(), o2.getClass())) {
            log.error("diff的两个对象类型不同, o1.class={}, o2.class={}", o1.getClass().toString(), o2.getClass().toString());
            return "";
        }
        if (!diffNode.hasChanges()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        diffNode.visit(new DiffNode.Visitor() {
            @Override
            public void node(DiffNode node, Visit visit) {
                DiffLogField diffLogFieldAnnotation = diffNode.getFieldAnnotation(DiffLogField.class);
                String filedLogName = diffLogFieldAnnotation.name();
                DiffNode.State state = node.getState();
                String template = "";
                switch (state) {
                    //todo 文案 properties 配置
                    case ADDED:
                        template = "增加了";
                        break;
                    case CHANGED:
                        template = "增加了";
                        break;
                    case REMOVED:
                        template = "增加了";
                        break;
                    default:
                        break;
                }

            }
        });

        return null;
    }
}
