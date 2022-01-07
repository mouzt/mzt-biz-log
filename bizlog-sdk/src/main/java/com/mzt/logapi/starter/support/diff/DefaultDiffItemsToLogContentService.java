package com.mzt.logapi.starter.support.diff;

import com.google.common.collect.Lists;
import com.mzt.logapi.service.IFunctionService;
import com.mzt.logapi.starter.configuration.LogRecordProperties;
import de.danielbechler.diff.node.DiffNode;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
            if (node.isRootNode()) {
                return;
            }
            DiffLogField diffLogFieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
            if (diffLogFieldAnnotation == null) {
                return;
            }
            if (node.getValueTypeInfo() != null) {
                //自定义对象类型直接进入对象里面
                return;
            }
            String filedLogName = diffLogFieldAnnotation.name();
            if (node.getParentNode() != null) {
                //获取对象的定语：比如：创建人的ID
                filedLogName = getParentFieldName(node) + filedLogName;
            }
            if (StringUtils.isEmpty(filedLogName)) {
                return;
            }

            boolean valueIsCollection = valueIsCollection(node, o1, o2);
            //是否是List类型的字段
            //获取值的转换函数
            String functionName = diffLogFieldAnnotation.function();
            DiffNode.State state = node.getState();
            String logContent = getDiffLogContent(filedLogName, node, state, o1, o2, functionName, valueIsCollection);
            if (!StringUtils.isEmpty(logContent)) {
                stringBuilder.append(logContent).append(logRecordProperties.getFieldSeparator());
            }
        });
        return stringBuilder.toString();
    }

    private boolean valueIsCollection(DiffNode node, Object sourceObject, Object targetObject) {
        if (sourceObject != null) {
            Object sourceValue = node.canonicalGet(sourceObject);
            if (sourceValue == null) {
                if (targetObject != null) {
                    return node.canonicalGet(targetObject) instanceof Collection;
                }
            }
            return sourceValue instanceof Collection;
        }
        return false;
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

    public String getDiffLogContent(String filedLogName, DiffNode node, DiffNode.State state, Object sourceObject, Object targetObject, String functionName, boolean valueIsCollection) {

        if (valueIsCollection) {
            List<Object> sourceList = getListValue(node, sourceObject);
            List<Object> targetList = getListValue(node, targetObject);
            List<Object> addItemList = listSubtract(targetList, sourceList);
            List<Object> delItemList = listSubtract(sourceList, targetList);
            String listAddContent = listToContent(functionName, addItemList);
            String listDelContent = listToContent(functionName, delItemList);
            return logRecordProperties.formatList(filedLogName, listAddContent, listDelContent);
        }
        switch (state) {
            case ADDED:
                return logRecordProperties.formatAdd(filedLogName, getFunctionValue(getFieldValue(node, targetObject), functionName));
            case CHANGED:
                return logRecordProperties.formatUpdate(filedLogName, getFunctionValue(getFieldValue(node, sourceObject), functionName), getFunctionValue(getFieldValue(node, targetObject), functionName));
            case REMOVED:
                return logRecordProperties.formatDeleted(filedLogName, getFunctionValue(getFieldValue(node, sourceObject), functionName));
            default:
                log.warn("diff log not support");
                return "";

        }
    }

    private List<Object> getListValue(DiffNode node, Object object) {
        Object fieldSourceValue = getFieldValue(node, object);
        //noinspection unchecked
        return fieldSourceValue == null ? Lists.newArrayList() : (List<Object>) fieldSourceValue;
    }

    private List<Object> listSubtract(List<Object> minuend, List<Object> subTractor) {
        List<Object> addItemList = new ArrayList<>(minuend);
        addItemList.removeAll(subTractor);
        return addItemList;
    }

    private String listToContent(String functionName, List<Object> addItemList) {
        StringBuilder listAddContent = new StringBuilder();
        if (!CollectionUtils.isEmpty(addItemList)) {
            for (Object item : addItemList) {
                listAddContent.append(getFunctionValue(item, functionName)).append(logRecordProperties.getFieldSeparator());
            }
        }
        return listAddContent.toString();
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
