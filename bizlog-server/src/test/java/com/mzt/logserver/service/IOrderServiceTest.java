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
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.update(1L, order);

    }

    @Test
    public void testCondition_打印日志() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.testCondition(1L, order, null);
        // 打印日志
    }

    @Test
    public void testCondition_不打印日志() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.testCondition(1L, order, "sss");
        // 打印日志
    }

    @Test
    public void testContextCallContext() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.testContextCallContext(1L, order);
        // 打印两条日志
    }
}