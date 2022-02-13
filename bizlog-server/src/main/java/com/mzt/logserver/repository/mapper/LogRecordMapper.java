package com.mzt.logserver.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mzt.logserver.repository.po.LogRecordPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LogRecordMapper extends BaseMapper<LogRecordPO> {
}
