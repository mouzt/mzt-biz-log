package com.mzt.logserver.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mzt.logserver.repository.po.LogRecordPO;

import java.util.List;

public interface LogRecordRepository extends IService<LogRecordPO> {
    List<LogRecordPO> queryLog(String bizNo, String type);

    List<LogRecordPO> queryLog(String bizNo, String type, String subType);

    /**
     * <p>根据操作日志的类型查找日志</p>
     *
     * @param type
     * @return 日志列表
     * @see com.mzt.logapi.starter.annotation.LogRecord#type()
     * @see LogRecordPO#type
     */
    List<LogRecordPO> queryLog(String type);

}
