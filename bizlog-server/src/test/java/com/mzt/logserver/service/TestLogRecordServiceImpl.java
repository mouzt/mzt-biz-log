package com.mzt.logserver.service;

import com.google.common.collect.Lists;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author muzhantong
 * create on 2020/4/29 4:34 下午
 */
@Component
@Slf4j
public class TestLogRecordServiceImpl implements ILogRecordService {

    private List<LogRecord> logRecordList = Lists.newArrayList();

    @Override
    public void record(LogRecord logRecord) {
        log.info("【logRecord test】log={}", logRecord);
        synchronized (this) {
            if (logRecord != null) {
                this.logRecordList.add(logRecord);
            }
        }

    }

    @Override
    public List<LogRecord> queryLog(String bizKey) {
        return logRecordList;
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo) {
        return logRecordList;
    }

    public void clean() {
        logRecordList.clear();
    }
}
