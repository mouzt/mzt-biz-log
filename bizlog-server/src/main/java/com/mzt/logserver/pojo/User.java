package com.mzt.logserver.pojo;

import com.mzt.logapi.starter.annotation.DiffLogField;
import com.mzt.logapi.starter.annotation.LogAllUnannotated;
import com.mzt.logapi.starter.annotation.LogIgnore;
import lombok.Data;

/**
 * @author: wulang
 **/
@Data
@LogAllUnannotated
public class User {

    private Long id;
    /**
     * 姓名
     */
    private String name;

    /**
     * 年龄
     */
    @LogIgnore
    private Integer age;

    /**
     * 性别
     */
    @DiffLogField(name = "性别", function = "SEX")
    private String sex;

    /**
     * 用户地址
     */
    private Address address;

    @Data
    public static class Address {
        /**
         * 省名称
         */
        private String provinceName;

        /**
         * 市名称
         */
        private String cityName;

        /**
         * 区/县名称
         */
        private String areaName;
    }
}
