package com.mzt.logapi.starter.support.diff;

import com.mzt.logapi.service.IFunctionService;
import com.mzt.logapi.starter.configuration.LogRecordProperties;
import de.danielbechler.diff.node.DiffNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author muzhantong
 * create on 2022/1/3 8:52 下午
 */
@Slf4j
@AllArgsConstructor
public class DefaultDiffItemsToLogContentService implements IDiffItemsToLogContentService {

    private final IFunctionService functionService;
    private final LogRecordProperties logRecordProperties;

    @Override
    public String toLogContent(DiffNode diffNode, final Object o1, final Object o2) {
        if (!diffNode.hasChanges()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        diffNode.visit((node, visit) -> {
            DiffLogField diffLogFieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
            String filedLogName = diffLogFieldAnnotation == null ? "" : diffLogFieldAnnotation.name();
            if (node.getParentNode() != null) {
                filedLogName = getParentFieldName(node) + filedLogName;
            }
            if (StringUtils.isEmpty(filedLogName)) {
                return;
            }
            if (node.getValueTypeInfo() != null) {
                return;
            }
            if (valueIsCollection(node, o1, o2)) {
                return;
            }

            String functionName = diffLogFieldAnnotation == null ? getParentFunction(node) : diffLogFieldAnnotation.function();
            DiffNode.State state = node.getState();
            String logContent = getDiffLogContent(filedLogName, node, state, o1, o2, functionName);
            if (!StringUtils.isEmpty(logContent)) {
                stringBuilder.append(logContent).append(logRecordProperties.getSeparator());
            }
        });
        return stringBuilder.toString();
    }

    private boolean valueIsCollection(DiffNode node, Object o1, Object o2) {
        if (o1 != null) {
            return node.canonicalGet(o1) instanceof Collection;
        }
        if (o2 != null) {
            return node.canonicalGet(o2) instanceof Collection;
        }
        return false;
    }

    private String getParentFunction(DiffNode node) {
        //针对List类型，需要从父节点获取function
        DiffLogField diffLogFieldAnnotation = node.getParentNode().getFieldAnnotation(DiffLogField.class);
        return diffLogFieldAnnotation == null ? "" : diffLogFieldAnnotation.function();
    }

    private String getParentFieldName(DiffNode node) {
        DiffNode parent = node.getParentNode();
        String fieldNamePrefix = "";
        while (parent != null) {
            DiffLogField diffLogFieldAnnotation = parent.getFieldAnnotation(DiffLogField.class);
            if (diffLogFieldAnnotation == null) {
                //父节点没有配置名称，不拼接
                parent = parent.getParentNode();
                continue;
            }
            fieldNamePrefix = diffLogFieldAnnotation.name().concat(logRecordProperties.getOfWord()).concat(fieldNamePrefix);
            parent = parent.getParentNode();
        }
        return fieldNamePrefix;
    }

    public String getDiffLogContent(String filedLogName, DiffNode node, DiffNode.State state, Object o1, Object o2, String functionName) {
        switch (state) {
            case ADDED:
                return logRecordProperties.formatAdd(filedLogName, null, getFunctionValue(getFieldValue(node, o2), functionName));
            case CHANGED:
                return logRecordProperties.formatUpdate(filedLogName, getFunctionValue(getFieldValue(node, o1), functionName), getFunctionValue(getFieldValue(node, o2), functionName));
            case REMOVED:
                return logRecordProperties.formatDeleted(filedLogName, getFunctionValue(getFieldValue(node, o1), functionName), null);
            default:
                log.warn("diff log not support");
                return "";

        }
    }

    private String getFunctionValue(Object canonicalGet, String functionName) {
        if (StringUtils.isEmpty(functionName)) {
            return canonicalGet.toString();
        }
        return functionService.apply(functionName, canonicalGet.toString());
    }

    private Object getFieldValue(DiffNode node, Object o2) {
        return node.canonicalGet(o2);
    }
}
