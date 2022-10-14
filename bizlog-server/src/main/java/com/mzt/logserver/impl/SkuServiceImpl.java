package com.mzt.logserver.impl;

import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.SkuService;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.infrastructure.logrecord.service.JoinTransactionLogRecordService;
import com.mzt.logserver.pojo.ObjectSku;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author wulang
 **/
@Slf4j
@Service
public class SkuServiceImpl implements SkuService {
    @Resource
    private JoinTransactionLogRecordService logRecordService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(
            fail = "创建SKU失败，失败原因：「{{#_errorMsg}}」",
            subType = LogRecordType.SKU,
            extra = "{{#sku.toString()}}",
            success = "新增SKU名称为{{#sku.skuName}}",
            type = LogRecordType.SKU, bizNo = "{{#sku.code}}")
    public Long createObjectSkuNoJoinTransaction(ObjectSku sku) {
        com.mzt.logapi.beans.LogRecord logRecord = getLogRecord(sku);
        logRecordService.record(logRecord);
        return null;
    }

    private com.mzt.logapi.beans.LogRecord getLogRecord(ObjectSku sku) {
        com.mzt.logapi.beans.LogRecord logRecord = new com.mzt.logapi.beans.LogRecord();
        logRecord.setTenant("test");
        logRecord.setType(LogRecordType.SKU);
        logRecord.setSubType(LogRecordType.SKU);
        logRecord.setBizNo(sku.getCode());
        logRecord.setOperator("operator");
        logRecord.setAction("新增SKU名称为" + sku.getSkuName());
        logRecord.setCreateTime(new Date());
        logRecord.setExtra(sku.getRemark());
        return logRecord;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(
            fail = "创建SKU失败，失败原因：「{{#_errorMsg}}」",
            subType = LogRecordType.SKU,
            extra = "{{#sku.toString()}}",
            success = "新增SKU名称为{{#sku.skuName}}",
            type = LogRecordType.SKU, bizNo = "{{#sku.code}}")
    public Long createObjectBusinessError(ObjectSku sku) {
        com.mzt.logapi.beans.LogRecord logRecord = getLogRecord(sku);
        logRecordService.record(logRecord);
        int i = 1 / 0;
        return null;
    }

    @Override
    @LogRecord(
            fail = "创建SKU失败，失败原因：「{{#_errorMsg}}」",
            subType = LogRecordType.SKU,
            extra = "{{#sku.toString()}}",
            success = "新增SKU名称为{{#sku.skuName}}",
            type = LogRecordType.SKU, bizNo = "{{#sku.code}}")
    public Long createObjectBusinessError2(ObjectSku sku) {
        com.mzt.logapi.beans.LogRecord logRecord = getLogRecord(sku);
        logRecordService.record(logRecord);
        int i = 1 / 0;
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @LogRecord(
            fail = "创建SKU失败，失败原因：「{{#_errorMsg}}」",
            subType = LogRecordType.SKU,
            extra = "{{#sku.getRemark()}}",
            success = "新增SKU名称为{{#sku.skuName}}",
            type = LogRecordType.SKU, bizNo = "{{#sku.code}}")
    public Long createObjectSkuNoJoinTransactionRollBack(ObjectSku sku) {
        com.mzt.logapi.beans.LogRecord logRecord = getLogRecord(sku);
        logRecord.setExtra("不回滚");
        logRecordService.record(logRecord);
        return null;
    }
}
