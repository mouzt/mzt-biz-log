package com.mzt.logserver;

import com.mzt.logserver.pojo.User;

/**
 * @author: wulang
 **/
public interface IUserService {
    boolean diffUser(User user, User newUser);
}
