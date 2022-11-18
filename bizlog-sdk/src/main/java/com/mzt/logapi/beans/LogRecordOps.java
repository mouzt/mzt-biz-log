package com.mzt.logapi.beans;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author muzhantong
 * create on 2020/4/29 3:27 下午
 */
@Data
@Builder
public class LogRecordOps {
    private String successLogTemplate;
    private String failLogTemplate;
    private String operatorId;
    private String type;
    private String bizNo;
    private String subType;
    private String extra;
    private String condition;
    private String isSuccess;
    /**
     * 字段变化的详细信息
     */
    private List<FieldActionDetails> fieldActionDetailsList;

    public void addFieldActionDetails(FieldActionDetails fieldActionDetails) {
        if (CollectionUtils.isEmpty(fieldActionDetailsList)) {
            fieldActionDetailsList = new ArrayList<>();
        }
        fieldActionDetailsList.add(fieldActionDetails);
    }
}
