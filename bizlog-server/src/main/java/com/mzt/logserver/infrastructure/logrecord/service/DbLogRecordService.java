package com.mzt.logserver.infrastructure.logrecord.service;

import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logserver.repository.LogRecordRepository;
import com.mzt.logserver.repository.po.LogRecordPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DbLogRecordService implements ILogRecordService {

    @Resource
    private LogRecordRepository logRecordRepository;

    @Override
    public void record(LogRecord logRecord) {
        logRecordRepository.save(LogRecordPO.from(logRecord));
    }

    @Override
    @Transactional(rollbackFor = Throwable.class, propagation = Propagation.REQUIRES_NEW)
    public void batchRecord(List<LogRecord> records) {
        Optional.ofNullable(records).ifPresent(x -> logRecordRepository.saveBatch(x
                .stream().map(LogRecordPO::from)
                .collect(Collectors.toCollection(ArrayList::new))));
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

    public List<LogRecord> queryLog(String type) {
        return LogRecordPO.from(logRecordRepository.queryLog(type));
    }

    public void clean() {

    }
}
