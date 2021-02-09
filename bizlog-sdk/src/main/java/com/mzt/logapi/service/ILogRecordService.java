package com.mzt.logapi.service;



import com.mzt.logapi.beans.LogRecord;

import java.util.List;

public interface ILogRecordService {
    /**
     * 保存log
     * @param logRecord
     */
    void record(LogRecord logRecord);

    /**
     * 返回最多100条记录
     * @param bizKey
     * @return
     */
    List<LogRecord> queryLog(String bizKey);

    /**
     * 返回最多100条记录
     * @param bizNo
     * @return
     */
    List<LogRecord> queryLogByBizNo(String bizNo);
}
