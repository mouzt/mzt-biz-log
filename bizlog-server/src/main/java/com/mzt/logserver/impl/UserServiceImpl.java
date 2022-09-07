package com.mzt.logserver.impl;

import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.IUserService;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author wulang
 **/
@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Override
    @LogRecord(success = "更新了用户信息{_DIFF{#user, #newUser}}",
            type = LogRecordType.USER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    public boolean diffUser(User user, User newUser) {
        return false;
    }
}
