package com.mzt.logserver.service;

import com.google.common.collect.Lists;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logserver.beans.Order;
import com.mzt.logserver.logrecord.TestLogRecordServiceImpl;
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
        orderService.createOrder(order);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,测试变量「内部变量测试」,下单结果:true");
        Assert.assertEquals(logRecord.getCategory(), "MANAGER_VIEW");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        logRecordService.clean();
    }

    @Test
    public void updateOrder() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.update(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单xxxx(10000),更新内容为...");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        logRecordService.clean();

    }

    @Test
    public void updateIdentity() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.identity(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单99,更新内容为...MT0000011");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        logRecordService.clean();
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
        order.setItems(Lists.newArrayList("123", "bbb"));


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
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    public void testDiff3() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        order.setItems(null);


        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        order1.setItems(Lists.newArrayList("123", "aaa"));
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【列表项】添加了【xxxx(123)，xxxx(aaa)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
        Assert.assertNotNull(logRecord.getDetail());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    public void testDiff4() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        order.setItems(Lists.newArrayList("123", "aaa"));


        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        order1.setItems(null);
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【列表项】删除了【xxxx(123)，xxxx(aaa)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
        logRecordService.clean();
    }

    @Test
    public void testDiff5() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);

        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单删除了【创建人的用户ID】：【9001】；删除了【创建人的用户姓名】：【用户1】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
        logRecordService.clean();
    }


    @Test
    public void testDiff2() {
        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff(null, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【订单ID】从【空】修改为【xxxx(88)】；【订单号】从【空】修改为【MT0000099】；");
        logRecordService.clean();
    }

    @Test
    public void testDiff_一个diff参数() {
        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff1(order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【订单ID】从【空】修改为【xxxx(88)】；【订单号】从【空】修改为【MT0000099】；");
        logRecordService.clean();
    }

    @Test
    public void testDiff_一个diff参数2() {

        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff2(order1);

        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单删除了【创建人的用户ID】：【9001】；删除了【创建人的用户姓名】：【用户1】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】；【订单号】从【MT0000011】修改为【MT0000099】；");
        logRecordService.clean();
    }

    @Test
    public void testCondition_打印日志() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testCondition(1L, order, null);
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(1, logRecordList.size());
        // 打印日志
        logRecordService.clean();
    }

    @Test
    public void testCondition_不打印日志() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testCondition(1L, order, "ss");
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(0, logRecordList.size());
        logRecordService.clean();
    }

    @Test
    public void testContextCallContext() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testContextCallContext(1L, order);
        // 打印两条日志
        List<LogRecord> logRecordList = logRecordService.queryLog("xxx");
        Assert.assertEquals(2, logRecordList.size());
        Assert.assertEquals(logRecordList.get(0).getAction(), "获取用户列表,内层方法调用人mzt");
        Assert.assertEquals(logRecordList.get(1).getAction(), "更新了订单xxxx(1),更新内容为..外层调用");
        logRecordService.clean();
    }
}