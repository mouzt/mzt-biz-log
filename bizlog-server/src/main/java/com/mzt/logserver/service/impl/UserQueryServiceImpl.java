package com.mzt.logserver.service.impl;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecordAnnotation;
import com.mzt.logserver.constants.LogRecordType;
import com.mzt.logserver.service.UserQueryService;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author muzhantong
 * create on 2021/2/10 11:13 上午
 */
@Service
public class UserQueryServiceImpl implements UserQueryService {

    @Override
    @LogRecordAnnotation(success = "获取用户列表,内层方法调用人{{#user}}", prefix = LogRecordType.ORDER, bizNo = "{{#user}}")
    public List<User> getUserList(List<String> userIds) {
        LogRecordContext.putVariable("user", "mzt");
        return null;
    }
}
