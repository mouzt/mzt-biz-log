package com.mzt.logapi.starter.diff;

import com.mzt.logapi.service.IFunctionService;
import com.mzt.logapi.starter.annotation.DIffLogIgnore;
import com.mzt.logapi.starter.annotation.DiffLogAllFields;
import com.mzt.logapi.starter.annotation.DiffLogField;
import com.mzt.logapi.starter.configuration.LogRecordProperties;
import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.selector.ElementSelector;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;

/**
 * @author muzhantong
 * create on 2022/1/3 8:52 下午
 */
@Slf4j
@Setter
@Getter
public class DefaultDiffItemsToLogContentService implements IDiffItemsToLogContentService, BeanFactoryAware, SmartInitializingSingleton {

    private IFunctionService functionService;
    private final LogRecordProperties logRecordProperties;
    private BeanFactory beanFactory;

    public DefaultDiffItemsToLogContentService(LogRecordProperties logRecordProperties) {
        this.logRecordProperties = logRecordProperties;
    }

    @Override
    public String toLogContent(DiffNode diffNode, final Object sourceObject, final Object targetObject) {
        if (!diffNode.hasChanges()) {
            return "";
        }
        DiffLogAllFields annotation = sourceObject.getClass().getAnnotation(DiffLogAllFields.class);
        StringBuilder stringBuilder = new StringBuilder();
        Set<DiffNode> set = new HashSet<>();
        diffNode.visit((node, visit) -> generateAllFieldLog(sourceObject, targetObject, stringBuilder, node, annotation, set));
        set.clear();
        return stringBuilder.toString().replaceAll(logRecordProperties.getFieldSeparator().concat("$"), "");
    }

    private void generateAllFieldLog(Object sourceObject, Object targetObject, StringBuilder stringBuilder, DiffNode node,
                                     DiffLogAllFields annotation, Set<DiffNode> set) {
        if (node.isRootNode() || node.getValueTypeInfo() != null || set.contains(node)) {
            return;
        }
        DIffLogIgnore logIgnore = node.getFieldAnnotation(DIffLogIgnore.class);
        if (logIgnore != null) {
            memorandum(node, set);
            return;
        }
        DiffLogField diffLogFieldAnnotation = node.getFieldAnnotation(DiffLogField.class);
        if (annotation == null && diffLogFieldAnnotation == null) {
            return;
        }
        String filedLogName = getFieldLogName(node, diffLogFieldAnnotation, annotation != null);
        if (StringUtils.isEmpty(filedLogName)) {
            return;
        }
        // 是否是容器类型的字段
        boolean valueIsContainer = valueIsContainer(node, sourceObject, targetObject);
        String functionName = diffLogFieldAnnotation != null ? diffLogFieldAnnotation.function() : "";
        String logContent = valueIsContainer
                ? getCollectionDiffLogContent(filedLogName, node, sourceObject, targetObject, functionName)
                : getDiffLogContent(filedLogName, node, sourceObject, targetObject, functionName);
        if (!StringUtils.isEmpty(logContent)) {
            stringBuilder.append(logContent).append(logRecordProperties.getFieldSeparator());
        }
        memorandum(node, set);
    }

    private void memorandum(DiffNode node, Set<DiffNode> set) {
        set.add(node);
        if (node.hasChildren()) {
            Field childrenField = ReflectionUtils.findField(DiffNode.class, "children");
            assert childrenField != null;
            ReflectionUtils.makeAccessible(childrenField);
            Map<ElementSelector, DiffNode> children = (Map<ElementSelector, DiffNode>) ReflectionUtils.getField(childrenField, node);
            assert children != null;
            for (DiffNode value : children.values()) memorandum(value, set);
        }
    }

    private String getFieldLogName(DiffNode node, DiffLogField diffLogFieldAnnotation, boolean isField) {
        String filedLogName = diffLogFieldAnnotation != null ? diffLogFieldAnnotation.name() : node.getPropertyName();
        if (node.getParentNode() != null) {
            //获取对象的定语：比如：创建人的ID
            filedLogName = getParentFieldName(node, isField) + filedLogName;
        }
        return filedLogName;
    }

    private boolean valueIsContainer(DiffNode node, Object sourceObject, Object targetObject) {
        if (sourceObject != null) {
            Object sourceValue = node.canonicalGet(sourceObject);
            if (sourceValue == null) {
                if (targetObject != null) {
                    return node.canonicalGet(targetObject) instanceof Collection || node.canonicalGet(targetObject).getClass().isArray();
                }
            } else {
                return sourceValue instanceof Collection || sourceValue.getClass().isArray();
            }
        }
        return false;
    }

    private String getParentFieldName(DiffNode node, boolean isField) {
        DiffNode parent = node.getParentNode();
        String fieldNamePrefix = "";
        while (parent != null) {
            DiffLogField diffLogFieldAnnotation = parent.getFieldAnnotation(DiffLogField.class);
            if ((diffLogFieldAnnotation == null && !isField) || parent.isRootNode()) {
                // 父节点没有配置名称且不用属性名映射，不拼接
                parent = parent.getParentNode();
                continue;
            }
            fieldNamePrefix = diffLogFieldAnnotation != null
                    ? diffLogFieldAnnotation.name().concat(logRecordProperties.getOfWord()).concat(fieldNamePrefix)
                    : parent.getPropertyName().concat(logRecordProperties.getOfWord()).concat(fieldNamePrefix);
            parent = parent.getParentNode();
        }
        return fieldNamePrefix;
    }

    public String getCollectionDiffLogContent(String filedLogName, DiffNode node, Object sourceObject, Object targetObject, String functionName) {
        //集合走单独的diff模板
        Collection<Object> sourceList = getListValue(node, sourceObject);
        Collection<Object> targetList = getListValue(node, targetObject);
        Collection<Object> addItemList = listSubtract(targetList, sourceList);
        Collection<Object> delItemList = listSubtract(sourceList, targetList);
        String listAddContent = listToContent(functionName, addItemList);
        String listDelContent = listToContent(functionName, delItemList);
        return logRecordProperties.formatList(filedLogName, listAddContent, listDelContent);
    }

    public String getDiffLogContent(String filedLogName, DiffNode node, Object sourceObject, Object targetObject, String functionName) {
        switch (node.getState()) {
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

    private Collection<Object> getListValue(DiffNode node, Object object) {
        Object fieldSourceValue = getFieldValue(node, object);
        //noinspection unchecked
        if (fieldSourceValue != null && fieldSourceValue.getClass().isArray()) {
            return new ArrayList<>(Arrays.asList((Object[]) fieldSourceValue));
        }
        return fieldSourceValue == null ? new ArrayList<>() : (Collection<Object>) fieldSourceValue;
    }

    private Collection<Object> listSubtract(Collection<Object> minuend, Collection<Object> subTractor) {
        Collection<Object> addItemList = new ArrayList<>(minuend);
        addItemList.removeAll(subTractor);
        return addItemList;
    }

    private String listToContent(String functionName, Collection<Object> addItemList) {
        StringBuilder listAddContent = new StringBuilder();
        if (!CollectionUtils.isEmpty(addItemList)) {
            for (Object item : addItemList) {
                listAddContent.append(getFunctionValue(item, functionName)).append(logRecordProperties.getListItemSeparator());
            }
        }
        return listAddContent.toString().replaceAll(logRecordProperties.getListItemSeparator() + "$", "");
    }

    private String getFunctionValue(Object canonicalGet, String functionName) {
        if (StringUtils.isEmpty(functionName)) {
            return canonicalGet.toString();
        }
        return functionService.apply(functionName, canonicalGet);
    }

    private Object getFieldValue(DiffNode node, Object o2) {
        return node.canonicalGet(o2);
    }

    @Override
    public void setBeanFactory(@NonNull BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterSingletonsInstantiated() {
        this.functionService = beanFactory.getBean(IFunctionService.class);
    }
}
