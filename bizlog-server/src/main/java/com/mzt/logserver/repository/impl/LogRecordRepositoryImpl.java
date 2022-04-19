package com.mzt.logserver.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mzt.logserver.repository.LogRecordRepository;
import com.mzt.logserver.repository.mapper.LogRecordMapper;
import com.mzt.logserver.repository.po.LogRecordPO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LogRecordRepositoryImpl extends ServiceImpl<LogRecordMapper, LogRecordPO> implements LogRecordRepository {

    @Override
    public List<LogRecordPO> queryLog(String bizNo, String type) {
        LogRecordMapper baseMapper = super.getBaseMapper();
        QueryWrapper<LogRecordPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(LogRecordPO::getType, type)
                .eq(LogRecordPO::getBizNo, bizNo)
                .orderByDesc(LogRecordPO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<LogRecordPO> queryLog(String bizNo, String type, String subType) {
        LogRecordMapper baseMapper = super.getBaseMapper();
        QueryWrapper<LogRecordPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(LogRecordPO::getType, type)
                .eq(LogRecordPO::getSubType, subType)
                .eq(LogRecordPO::getBizNo, bizNo)
                .orderByDesc(LogRecordPO::getCreateTime);
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public List<LogRecordPO> queryLog(String type) {
        QueryWrapper<LogRecordPO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(LogRecordPO::getType, type);
        return baseMapper.selectList(wrapper);
    }

}
