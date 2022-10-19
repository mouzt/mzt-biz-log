package com.mzt.logserver.pojo;

import lombok.Data;

import java.util.List;

/**
 * @author wulang
 **/
@Data
public class ObjectSku {

    private String skuName;

    private Long templateId;

    private String code;

    private String remark;

    private String taxCategoryNo;

    private String measureUnit;

    private List<Long> children;

}
