package com.mzt.logserver.service.impl;

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
    public List<User> getUserList(List<String> userIds) {
        return null;
    }
}
