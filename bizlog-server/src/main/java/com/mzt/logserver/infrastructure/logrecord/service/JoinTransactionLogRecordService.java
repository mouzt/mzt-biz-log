package com.mzt.logserver.infrastructure.logrecord.service;

import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logserver.repository.LogRecordRepository;
import com.mzt.logserver.repository.po.LogRecordPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author wulang
 **/
@Slf4j
@Service("JoinTransactionLogRecordService")
public class JoinTransactionLogRecordService implements ILogRecordService {

    @Resource
    private LogRecordRepository logRecordRepository;

    @Override
    public void record(LogRecord logRecord) {
        if (Objects.equals(logRecord.getExtra(), "日志错误_事务不回滚")) {
            int i = 1 / 0;
        }
        logRecordRepository.save(LogRecordPO.from(logRecord));
        if (Objects.equals(logRecord.getExtra(), "日志错误_事务回滚")) {
            int i = 1 / 0;
        }
    }

    @Override
    public List<LogRecord> queryLog(String bizNo, String type) {
        List<LogRecordPO> logRecordPOS = logRecordRepository.queryLog(bizNo, type);
        return LogRecordPO.from(logRecordPOS);
    }

    @Override
    public List<LogRecord> queryLogByBizNo(String bizNo, String type, String subType) {
        List<LogRecordPO> logRecordPOS = logRecordRepository.queryLog(bizNo, type, subType);
        return LogRecordPO.from(logRecordPOS);
    }

    public void clean() {

    }
}
