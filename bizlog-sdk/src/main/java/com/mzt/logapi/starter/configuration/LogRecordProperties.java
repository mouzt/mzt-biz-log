package com.mzt.logapi.starter.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author muzhantong
 * create on 2022/1/6 3:26 下午
 */
@ConfigurationProperties(prefix = "mzt.log.record")
@Data
public class LogRecordProperties {

    private final String FIELD_PLACEHOLDER = "{{#fieldName}}";
    private final String SOURCE_VALUE_PLACEHOLDER = "{{#sourceValue}}";
    private final String TARGET_VALUE_PLACEHOLDER = "{{#targetValue}}";

    private String addTemplate = "【" + FIELD_PLACEHOLDER + "】从【空】修改为【" + TARGET_VALUE_PLACEHOLDER + "】";
    private String updateTemplate = "【" + FIELD_PLACEHOLDER + "】从【" + SOURCE_VALUE_PLACEHOLDER + "】修改为【" + TARGET_VALUE_PLACEHOLDER + "】";
    private String deleteTemplate = "删除了【" + FIELD_PLACEHOLDER + "】：【" + SOURCE_VALUE_PLACEHOLDER + "】";
    private String separator = "；";
    private String ofWord = "的";


    public void setAddTemplate(String template) {
        validatePlaceHolder(template);
        this.addTemplate = template;
    }

    public void setUpdateTemplate(String template) {
        validatePlaceHolder(template);
        this.updateTemplate = template;
    }

    public void setDeleteTemplate(String template) {
        validatePlaceHolder(template);
        this.deleteTemplate = template;
    }

    private void validatePlaceHolder(String template) {
        if (!template.contains(FIELD_PLACEHOLDER) && !template.contains(SOURCE_VALUE_PLACEHOLDER) && !template.contains(TARGET_VALUE_PLACEHOLDER)) {
            throw new IllegalArgumentException("请检查 logRecord template, 模板需要配置 {{#fieldName}},{{#sourceValue}},{{#targetValue}} 三个变量中的任何一个");
        }
    }

    public String formatAdd(String fieldName, Object sourceValue, Object targetValue) {
        return addTemplate.replace(FIELD_PLACEHOLDER, fieldName)
                .replace(SOURCE_VALUE_PLACEHOLDER, String.valueOf(sourceValue))
                .replace(TARGET_VALUE_PLACEHOLDER, String.valueOf(targetValue));
    }

    public String formatUpdate(String fieldName, Object sourceValue, Object targetValue) {
        return updateTemplate.replace(FIELD_PLACEHOLDER, fieldName)
                .replace(SOURCE_VALUE_PLACEHOLDER, String.valueOf(sourceValue))
                .replace(TARGET_VALUE_PLACEHOLDER, String.valueOf(targetValue));
    }

    public String formatDeleted(String fieldName, Object sourceValue, Object targetValue) {
        return deleteTemplate.replace(FIELD_PLACEHOLDER, fieldName)
                .replace(SOURCE_VALUE_PLACEHOLDER, String.valueOf(sourceValue))
                .replace(TARGET_VALUE_PLACEHOLDER, String.valueOf(targetValue));
    }

}
