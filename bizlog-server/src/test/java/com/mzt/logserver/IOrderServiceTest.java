package com.mzt.logserver;

import cn.hutool.extra.spring.SpringUtil;
import com.google.common.collect.Lists;
import com.mzt.logapi.beans.CodeVariableType;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.starter.support.aop.LogRecordInterceptor;
import com.mzt.logserver.impl.OrderServiceImpl;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.infrastructure.logrecord.service.DbLogRecordService;
import com.mzt.logserver.pojo.Order;
import com.mzt.logserver.pojo.User;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.jdbc.Sql;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

public class IOrderServiceTest extends BaseTest {
    @Resource
    private IOrderService orderService;
    @Resource
    private DbLogRecordService logRecordService;

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.createOrder(order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,测试变量「内部变量测试」,下单结果:true");
        Assert.assertEquals(logRecord.getSubType(), "MANAGER_VIEW");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        Assert.assertFalse(logRecord.isFail());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrders() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.createOrders(order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true");
        Assert.assertEquals(logRecord.getSubType(), "MANAGER_VIEW");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        Assert.assertFalse(logRecord.isFail());
        List<LogRecord> userLogRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.USER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord userLogRecord = userLogRecordList.get(0);
        Assert.assertEquals(userLogRecord.getAction(), "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true");
        Assert.assertEquals(userLogRecord.getSubType(), "USER_VIEW");
        Assert.assertEquals("", userLogRecord.getExtra());
        Assert.assertEquals(userLogRecord.getBizNo(), order.getOrderNo());
        Assert.assertFalse(userLogRecord.isFail());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrderMonitor() {

        for (int i = 0; i < 100; i++) {
            Order order = new Order();
            order.setOrderNo("MT0000011");
            order.setProductName("超值优惠红烧肉套餐");
            order.setPurchaseName("张三");
            orderService.createOrder(order);

        }
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder_interface() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.createOrder_interface(order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "张三下了一个订单,购买商品「超值优惠红烧肉套餐」,测试变量「内部变量测试」,下单结果:true");
        Assert.assertEquals(logRecord.getSubType(), "MANAGER_VIEW");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        Assert.assertFalse(logRecord.isFail());
        logRecordService.clean();
    }

    @Test(expected = RuntimeException.class)
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder_fail() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        try {
            orderService.createOrder_fail(order);
        } finally {
            List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
            Assert.assertEquals(1, logRecordList.size());
            LogRecord logRecord = logRecordList.get(0);
            Assert.assertEquals(logRecord.getAction(), "创建订单失败，失败原因：「测试fail」");
            Assert.assertEquals(logRecord.getSubType(), "MANAGER_VIEW");
            Assert.assertNotNull(logRecord.getExtra());
            Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
            Assert.assertTrue(logRecord.isFail());
            logRecordService.clean();
        }
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateOrderBefore() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.updateBefore(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单xxxx(99),更新内容为...");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        logRecordService.clean();

    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateOrderAfter() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.updateAfter(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单xxxx(10000),更新内容为...");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        logRecordService.clean();

    }

    @Test
    public void updateDollar() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.dollar(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "测试刀了符号10$,/666哈哈哈");
        logRecordService.clean();

    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void updateIdentity() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.identity(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单99,更新内容为...MT0000011");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff1() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
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

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff3() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
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

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【列表项】添加了【xxxx(123)，xxxx(aaa)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff4() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
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

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【列表项】删除了【xxxx(123)，xxxx(aaa)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff5() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
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

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单删除了【创建人的用户ID】：【9001】；删除了【创建人的用户姓名】：【用户1】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        logRecordService.clean();
    }


    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff2() {
        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff(null, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog(order1.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【订单ID】从【空】修改为【xxxx(88)】；【订单号】从【空】修改为【MT0000099】");
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff_不记录() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);
        order.setItems(Lists.newArrayList("123", "bbb"));

        Order order1 = new Order();
        order1.setOrderId(99L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("超值优惠红烧肉套餐");
        order1.setPurchaseName("张三");
        Order.UserDO userDO1 = new Order.UserDO();
        userDO1.setUserId(9001L);
        userDO1.setUserName("用户1");
        order1.setCreator(userDO1);
        order1.setItems(Lists.newArrayList("123", "bbb"));
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(0, logRecordList.size());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff_一个diff参数() {
        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff1(order1);

        List<LogRecord> logRecordList = logRecordService.queryLog(order1.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【订单ID】从【空】修改为【xxxx(88)】；【订单号】从【空】修改为【MT0000099】");
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testDiff_一个diff参数2() {

        Order order1 = new Order();
        order1.setOrderId(88L);
        order1.setOrderNo("MT0000099");
        order1.setProductName("麻辣烫套餐");
        order1.setPurchaseName("赵四");
        orderService.diff2(order1);

        List<LogRecord> logRecordList = logRecordService.queryLog(order1.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单删除了【创建人的用户ID】：【9001】；删除了【创建人的用户姓名】：【用户1】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCondition_数组修改() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);
        order.setItems(Lists.newArrayList("123", "bbb"));
        order.setExtInfo(new String[]{"p", "k"});

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
        order1.setExtInfo(new String[]{"q", "k"});
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【拓展信息】添加了【q】删除了【p】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCondition_数组增加() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
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
        order1.setExtInfo(new String[]{"q", "k"});
        orderService.diff(order, order1);

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【拓展信息】添加了【q，k】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCondition_数组删除() {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);
        order.setItems(Lists.newArrayList("123", "bbb"));
        order.setExtInfo(new String[]{"p", "k"});

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

        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        LogRecord logRecord = logRecordList.get(0);
        Assert.assertEquals(logRecord.getAction(), "更新了订单【创建人的用户ID】从【9001】修改为【9002】；【创建人的用户姓名】从【用户1】修改为【用户2】；【拓展信息】删除了【p，k】；【列表项】添加了【xxxx(aaa)】删除了【xxxx(bbb)】；【订单ID】从【xxxx(99)】修改为【xxxx(88)】");
        Assert.assertNotNull(logRecord.getExtra());
        Assert.assertEquals(logRecord.getOperator(), "111");
        Assert.assertEquals(logRecord.getBizNo(), order1.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCondition_打印日志() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testCondition(1L, order, null);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        // 打印日志
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testCondition_不打印日志() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testCondition(1L, order, "ss");
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(0, logRecordList.size());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testContextCallContext() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testContextCallContext(1L, order);
        // 打印两条日志
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(2, logRecordList.size());
        Assert.assertEquals(logRecordList.get(1).getAction(), "获取用户列表,内层方法调用人mzt");
        Assert.assertEquals(logRecordList.get(0).getAction(), "更新了订单xxxx(1),更新内容为..外层调用");
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testSubTypeSpEl() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testSubTypeSpEl(1L, order);
        // 打印两条日志
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        Assert.assertEquals(logRecordList.get(0).getSubType(), order.getOrderNo());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testVariableInfo() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testVariableInfo(1L, order);
        // 打印两条日志
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        Map<CodeVariableType, Object> codeVariable = logRecordList.get(0).getCodeVariable();
        Assert.assertEquals(codeVariable.size(), 2);
        Assert.assertEquals(codeVariable.get(CodeVariableType.ClassName), OrderServiceImpl.class.toString());
        Assert.assertEquals(codeVariable.get(CodeVariableType.MethodName), "testVariableInfo");
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testResultOnSuccess() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testResultOnSuccess(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        Assert.assertFalse(logRecordList.get(0).isFail());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testResultOnFail() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testResultOnFail(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(1, logRecordList.size());
        Assert.assertTrue(logRecordList.get(0).isFail());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testResultNoLog() {
        Order order = new Order();
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        orderService.testResultNoLog(1L, order);
        List<LogRecord> logRecordList = logRecordService.queryLog(order.getOrderNo(), LogRecordType.ORDER);
        Assert.assertEquals(0, logRecordList.size());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void fixedCopy() {
        String text = "text";
        orderService.fixedCopy(text);
        List<LogRecord> logRecordList = logRecordService.queryLog(text, LogRecordType.USER);
        Assert.assertEquals(1, logRecordList.size());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void fixedCopy2() {
        // 记录日志
        LogRecordInterceptor bean = SpringUtil.getBean(LogRecordInterceptor.class);
        bean.setDiffLog(true);
        User user = new User();
        user.setName("张三");
        User oldUser = new User();
        oldUser.setName("张三");
        orderService.fixedCopy2(user, oldUser);
        List<LogRecord> logRecordList = logRecordService.queryLog(user.getName(), LogRecordType.USER);
        Assert.assertEquals(1, logRecordList.size());
        logRecordService.clean();
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void fixedCopy3() {
        // 不记录日志
        LogRecordInterceptor bean = SpringUtil.getBean(LogRecordInterceptor.class);
        bean.setDiffLog(false);
        User user = new User();
        user.setName("张三");
        User oldUser = new User();
        oldUser.setName("张三");
        orderService.fixedCopy2(user, oldUser);
        List<LogRecord> logRecordList = logRecordService.queryLog(user.getName(), LogRecordType.USER);
        Assert.assertEquals(0, logRecordList.size());
        logRecordService.clean();
    }
}