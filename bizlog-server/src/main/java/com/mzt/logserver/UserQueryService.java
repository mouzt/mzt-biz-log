package com.mzt.logserver;

import org.apache.catalina.User;

import java.util.List;

/**
 * @author muzhantong
 * create on 2021/2/10 11:13 上午
 */
public interface UserQueryService {
    List<User> getUserList(List<String> userIds);
}
