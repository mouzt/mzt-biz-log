package com.mzt.logserver.impl;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.UserQueryService;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author muzhantong
 * create on 2021/2/10 11:13 上午
 */
@Service
@Slf4j
public class UserQueryServiceImpl implements UserQueryService {

    @Override
    @LogRecord(success = "获取用户列表,内层方法调用人{{#user}}", type = LogRecordType.ORDER, bizNo = "MT0000011")
    public List<User> getUserList(List<String> userIds) {
        LogRecordContext.putVariable("user", "mzt");
        return null;
    }
}
