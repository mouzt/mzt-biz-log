package com.mzt.logserver.impl;

import cn.hutool.core.bean.BeanUtil;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.IOrderService;
import com.mzt.logserver.IUserService;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.Order;
import com.mzt.logserver.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author wulang
 **/
@Slf4j
@Service
public class UserServiceImpl extends AbsUserServiceImpl implements IUserService {

    @Autowired
    private IOrderService orderService;

    @Override
    @LogRecord(success = "更新了用户信息{_DIFF{#user, #newUser}}",
            type = LogRecordType.USER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    public boolean diffUser(User user, User newUser) {
        return false;
    }

    @Override
    @LogRecord(success = "更新{{#user.name}}用户积分信息",
            type = LogRecordType.USER, bizNo = "{{#user.id}}",
            extra = "{{#order.toString()}}")
    public boolean testGlobalVariable(User user, Order order) {
        LogRecordContext.putGlobalVariable("user", user);
        orderService.testGlobalVariable(order);
        return false;
    }

    @Override
    @LogRecord(success = "更新了用户信息{_DIFF{#newUser}}",
            type = LogRecordType.USER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    public boolean testGlobalVariableDiff(User newUser) {

        return false;
    }

    @Override
    @LogRecord(success = "更新{{#user.name}}用户积分信息",
            type = LogRecordType.USER, bizNo = "{{#user.id}}",
            extra = "{{#order.toString()}}")
    public boolean testGlobalVariableCover(User user, Order order) {
        User newUser = new User();
        LogRecordContext.putGlobalVariable("user", user);
        BeanUtil.copyProperties(user, newUser);
        newUser.setName("李四");
        orderService.testGlobalVariableCover(order, newUser);
        return false;
    }

    @Override
    @LogRecord(success = "更新了用户信息{_DIFF{#user, #newUser}}",
            type = LogRecordType.ORDER, bizNo = "{{#newUser.id}}",
            extra = "{{#newUser.toString()}}")
    public boolean testInterfaceAndAbstract(User user, User newUser) {
        return false;
    }
}
