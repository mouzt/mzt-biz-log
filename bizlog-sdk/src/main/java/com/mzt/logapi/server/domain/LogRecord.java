package com.mzt.logapi.server.domain;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;

import java.util.Date;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LogRecord {
    private Integer id;

    @NotBlank(message = "bizKey required")
    @Length(max = 200, message = "appKey max length is 200")
    private String bizKey;
    @NotBlank(message = "bizNo required")
    @Length(max = 200, message = "bizNo max length is 200")
    private String bizNo;

    @NotBlank(message = "operator required")
    @Length(max = 63, message = "operator max length 63")
    private String operator;
    @NotBlank(message = "operator required")
    @Length(max = 63, message = "operator max length 63")
    private String operatorId;
    @NotBlank(message = "opAction required")
    @Length(max = 511, message = "operator max length 511")
    private String action;

    private String category;


    @NotBlank(message = "appKey required")
    @Length(max = 64, message = "appKey max length is 64")
    private String appKey;

    private Date createTime;

    @Override
    public String toString() {
        return "LogRecord{" +
                "id=" + id +
                ", bizKey='" + bizKey + '\'' +
                ", bizNo='" + bizNo + '\'' +
                ", operator='" + operator + '\'' +
                ", operatorId='" + operatorId + '\'' +
                ", action='" + action + '\'' +
                ", category='" + category + '\'' +
                ", appKey='" + appKey + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}