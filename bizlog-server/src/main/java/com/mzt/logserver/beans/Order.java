package com.mzt.logserver.beans;

import com.fastobject.diff.DiffLog;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @author muzhantong
 * create on 2020/6/12 11:06 上午
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @DiffLog(name = "订单ID")
    private Long orderId;
    @DiffLog(name = "订单号")
    private String orderNo;
    private String purchaseName;
    private String productName;
    @DiffLog(name = "创建时间", dateFormat = "yyyy-dd-MM hh:mm:ss")
    private Date createTime;

    @DiffLog(name = "创建人")
    private UserDO creator;

    List<String> items;

    @Data
    public static class UserDO {
        @DiffLog(name = "用户ID")
        private Long userId;
        @DiffLog(name = "用户姓名")
        private String userName;
    }
}
