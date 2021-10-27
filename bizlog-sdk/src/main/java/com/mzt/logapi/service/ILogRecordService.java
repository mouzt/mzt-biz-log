package com.mzt.logapi.service;


import com.mzt.logapi.beans.LogRecord;

import java.util.List;

public interface ILogRecordService<T extends LogRecord<?>> {
    /**
     * 保存log
     *
     * @param logRecord 日志实体
     */
    void record(T logRecord);

    /**
     * 返回最多100条记录
     *
     * @param bizKey 日志前缀+bizNo
     * @return 操作日志列表
     */
    List<T> queryLog(String bizKey);

    /**
     * 返回最多100条记录
     *
     * @param bizNo 业务标识
     * @return 操作日志列表
     */
    List<T> queryLogByBizNo(String bizNo);
}
