package com.mzt.logserver.service;

import com.mzt.logserver.beans.Order;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;

public class IOrderServiceTest extends BaseTest {
    @Resource
    private IOrderService orderService;

    @Test
    public void createOrder() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.createOrder(order);
        Assert.assertTrue(ret);

    }

    @Test
    public void updateOrder() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.update(1L, order);

    }
}