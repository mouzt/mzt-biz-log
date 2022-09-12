package com.mzt.logserver.pojo;

import lombok.Data;

/**
 * @author wulang
 **/
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;
    /**
     * 错误的状态信息
     */
    private String message;
    /**
     * 数据
     */
    private T data;

    public Result(Integer status, String message, T data) {
        this.code = status;
        this.message = message;
        this.data = data;
    }
}