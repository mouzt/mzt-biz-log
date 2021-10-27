package com.mzt.logapi.service.impl;

import com.mzt.logapi.beans.StringLogRecord;
import com.mzt.logapi.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author muzhantong
 * create on 2020/4/29 4:34 下午
 */
@Slf4j
public class DefaultLogRecordServiceImpl implements ILogRecordService<StringLogRecord> {

//    @Resource
//    private LogRecordMapper logRecordMapper;
//
//    @Override
////    @Transactional(propagation = Propagation.REQUIRES_NEW)
//    public void record(LogRecord<String> logRecord) {
//        log.info("【logRecord】log={}", logRecord);
////        logRecordMapper.insertSelective(logRecord);
//    }
//
//    @Override
//    public List<LogRecord<String>> queryLog(String bizKey) {
////        return logRecordMapper.queryByBizKey(bizKey);
//        return Lists.newArrayList();
//    }
//
//    @Override
//    public List<LogRecord<String>> queryLogByBizNo(String bizNo) {
//
////        return logRecordMapper.queryByBizNo(bizNo);
//        return Lists.newArrayList();
//    }




    @Override
    public void record(StringLogRecord logRecord) {
        log.info("【logRecord】log={}", logRecord);
    }

    @Override
    public List<StringLogRecord> queryLog(String bizKey) {
        return null;
    }

    @Override
    public List<StringLogRecord> queryLogByBizNo(String bizNo) {
        return null;
    }
}
