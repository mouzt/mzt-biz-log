package com.mzt.logserver.impl;

import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.User;

/**
 * @author wulang
 **/
public abstract class AbsUserServiceImpl {
    public boolean testAbstract(User user, User newUser) {
        return false;
    }

    public boolean testInterface(User user, User newUser) {
        return false;
    }

    @LogRecord(success = "更新了用户信息{_DIFF{#user, #newUser}}",
            type = LogRecordType.USER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    @LogRecord(success = "更新了用户信息{_DIFF{#user, #newUser}}",
            type = LogRecordType.ORDER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    public boolean testAbstracts(User user, User newUser) {
        return false;
    }

    @LogRecord(success = "更新了用户信息{_DIFF{#user, #newUser}}",
            type = LogRecordType.ORDER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    public boolean testInterfaceAndAbstract2(User user, User newUser) {
        return false;
    }
}