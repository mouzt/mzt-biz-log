package com.mzt.logapi.service.impl;

import com.google.common.collect.Lists;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author muzhantong
 * create on 2020/4/29 4:34 下午
 */
@Slf4j
public class DefaultLogRecordServiceImpl implements ILogRecordService {

//    @Resource
//    private LogRecordMapper logRecordMapper;

    @Override
//    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(LogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
//        logRecordMapper.insertSelective(logRecord);
    }

    @Override
    public List<LogRecord> queryLog(String bizKey) {
//        return logRecordMapper.queryByBizKey(bizKey);
        return Lists.newArrayList();
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo) {

//        return logRecordMapper.queryByBizNo(bizNo);
        return Lists.newArrayList();
    }
}
