package com.mzt.logserver.pojo;

import com.mzt.logapi.starter.annotation.DiffLogField;
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
    @DiffLogField(name = "订单ID", function = "ORDER")
    private Long orderId;
    @DiffLogField(name = "订单号")
    private String orderNo;
    private String purchaseName;
    private String productName;
    @DiffLogField(name = "创建时间")
    private Date createTime;

    @DiffLogField(name = "创建人")
    private UserDO creator;
    @DiffLogField(name = "更新人")
    private UserDO updater;
    @DiffLogField(name = "列表项", function = "ORDER")
    private List<String> items;

    @DiffLogField(name = "拓展信息", function = "extInfo")
    private String[] extInfo;

    @Data
    public static class UserDO {
        @DiffLogField(name = "用户ID")
        private Long userId;
        @DiffLogField(name = "用户姓名")
        private String userName;
    }
}
