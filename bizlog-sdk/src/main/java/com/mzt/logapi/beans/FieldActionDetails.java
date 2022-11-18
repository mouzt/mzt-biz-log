package com.mzt.logapi.beans;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author 吴昊
 */
@Getter
@Setter
@ToString
public class FieldActionDetails {
    /**
     * 字段属性名
     */
    private String fieldName;
    /**
     * 字段别名 业务上的名字
     */
    private String fieldAlias;
    /**
     * 字段更新后的值
     */
    private Object fieldNewValue;
    /**
     * 字段更新前的值
     */
    private Object fieldOldValue;
}
