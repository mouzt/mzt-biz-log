package com.mzt.logserver.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mzt.logserver.repository.po.LogRecordPO;

import java.util.List;

public interface LogRecordRepository extends IService<LogRecordPO> {
    List<LogRecordPO> queryLog(String bizNo, String type);

    List<LogRecordPO> queryLog(String bizNo, String type, String subType);
}
