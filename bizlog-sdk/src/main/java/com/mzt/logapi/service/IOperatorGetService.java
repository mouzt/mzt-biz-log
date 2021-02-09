package com.mzt.logapi.service;


import com.mzt.logapi.beans.Operator;

/**
 * @author muzhantong
 * create on 2020/4/29 5:39 下午
 */
public interface IOperatorGetService {

    /**
     * 可以在里面外部的获取当前登陆的用户，比如UserContext.getCurrentUser()
     *
     * @return 转换成Operator返回
     */
    Operator getUser();
}
