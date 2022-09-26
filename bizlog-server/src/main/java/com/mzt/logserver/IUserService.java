package com.mzt.logserver;

import com.mzt.logserver.pojo.Order;
import com.mzt.logserver.pojo.User;

/**
 * @author wulang
 **/
public interface IUserService {
    boolean diffUser(User user, User newUser);

    boolean testGlobalVariable(User user, Order order);

    boolean testGlobalVariableCover(User user, Order order);
}
