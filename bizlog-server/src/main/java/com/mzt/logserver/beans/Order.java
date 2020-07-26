package com.mzt.logserver.beans;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author muzhantong
 * create on 2020/6/12 11:06 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    private Long orderId;
    private String orderNo;
    private String purchaseName;
    private String productName;
    private Date createTime;
}
