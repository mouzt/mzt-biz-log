package com.mzt.logapi.starter.support.diff;

import de.danielbechler.diff.node.DiffNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * @author muzhantong
 * create on 2022/1/3 8:52 下午
 */
@Slf4j
public class DefaultDiffItemsToLogContentService implements IDiffItemsToLogContentService {

    @Override
    public String toLogContent(DiffNode diffNode, final Object o1, final Object o2) {
        if (!diffNode.hasChanges()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        diffNode.visit((node, visit) -> {
            DiffLogField diffLogFieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
            if (diffLogFieldAnnotation == null) {
                return;
            }
            String filedLogName = diffLogFieldAnnotation.name();
            String functionName = diffLogFieldAnnotation.function();
            DiffNode.State state = node.getState();
            String logContent = getDiffLogContent(filedLogName, node, state, o1, o2, functionName);
            if (!StringUtils.isEmpty(logContent)) {
                //todo /n 可以配置
                stringBuilder.append(logContent).append("/n");
            }
        });
        return stringBuilder.toString();
    }

    public String getDiffLogContent(String filedLogName, DiffNode node, DiffNode.State state, Object o1, Object o2, String functionName) {
        //todo 根据functionName 获取真正的值
        switch (state) {
            case ADDED:
                return String.format("【%s】添加了【%s】", filedLogName, node.canonicalGet(o2));
            case CHANGED:
                return String.format("【%s】从【%s】修改为【%s】", filedLogName, node.canonicalGet(o1), node.canonicalGet(o2));
            case REMOVED:
                return String.format("删除了【%s】：【%s】", filedLogName, node.canonicalGet(o1));
            default:
                log.warn("diff log not support");
                return "";

        }
    }
}
