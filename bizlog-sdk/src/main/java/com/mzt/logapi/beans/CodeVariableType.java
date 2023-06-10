package com.mzt.logapi.beans;

/**
 * @author muzhantong
 * create on 2022/3/31 11:39 PM
 */
public enum CodeVariableType {
    ClassName,
    MethodName,
    /**
     * USER-AGENT
     */
    USER_AGENT,
    /**
     * 请求ip
     */
    REQUEST_IP,
    /**
     * 请求方法
     */
    REQUEST_METHOD,
    /**
     * 请求uri
     */
    REQUEST_URL,
    /**
     * 请求参数
     */
    REQUEST_PARAM,
    /**
     * CONTENT_TYPE
     *
     */
    CONTENT_TYPE,
    /**
     * 请求耗时
     */
    COST_TIME,
    ;
}
