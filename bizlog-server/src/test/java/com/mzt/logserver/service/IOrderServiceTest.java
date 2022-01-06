package com.mzt.logserver.service;

import com.google.common.collect.Lists;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logserver.beans.Order;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

public class IOrderServiceTest extends BaseTest {
    @Resource
    private IOrderService orderService;
    @Resource
    private TestLogRecordServiceImpl logRecordService;

    @Test
    public void createOrder() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.createOrder(order);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,测试变量「内部变量测试」,下单结果:true");
        Assert.assertEquals(logRecord.getCategory(), "MANAGER_VIEW");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertNull(logRecord.getOperator());
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
    }

    @Test
    public void updateOrder() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.update(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单xxxx(10000),更新内容为...");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());

    }

    @Test
    public void updateIdentity() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        boolean ret = orderService.identity(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单99,更新内容为...MT0000011");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
    }

    @Test
    public void testDiff1() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);
        order.setItems(Lists.newArrayList("123"));


        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        Order.UserDO userDO1 = new Order.UserDO();
        userDO1.setUserId(9002L);
        userDO1.setUserName("用户2");
        order1.setCreator(userDO1);
        order1.setItems(Lists.newArrayList("123", "aaa"));
        boolean ret = orderService.diff(order, order1);

//        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
//        Assert.assertEquals(1, logRecordList.size());
//        LogRecord logRecord = logRecordList.get(0);
//        Assert.assertEquals(logRecord.getAction(), "更新了订单【用户ID】从【9001】修改为【9002】；【用户姓名】从【用户1】修改为【用户2】；【列表项】从【[123]】修改为【[123, aaa]】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
//        Assert.assertNotNull(logRecord.getDetail());
//        Assert.assertEquals(logRecord.getOperator(), "111");
//        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
    }

    @Test
    public void testDiff2() {
        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        boolean ret = orderService.diff(null, order1);

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