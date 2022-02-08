package com.mzt.logserver.service.impl;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecordAnnotation;
import com.mzt.logserver.beans.Order;
import com.mzt.logserver.constants.LogRecordType;
import com.mzt.logserver.service.IOrderService;
import com.mzt.logserver.service.UserQueryService;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author muzhantong
 * create on 2020/6/12 11:08 上午
 */
@Service
@Slf4j
public class OrderServiceImpl implements IOrderService {

    @Resource
    private UserQueryService userQueryService;

    /*'张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true' */
    @Override
    @LogRecordAnnotation(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            category = "MANAGER_VIEW",
            detail = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }


    /*'张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true' */
    @Override
    @LogRecordAnnotation(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            category = "MANAGER_VIEW",
            detail = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder_fail(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        if (order.getProductName().length() > 1) {
            throw new RuntimeException("测试fail");
        }
        return true;
    }

    @Override
    @LogRecordAnnotation(success = "更新了订单{ORDER{#order.orderId}},更新内容为...",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            detail = "{{#order.toString()}}")
    public boolean update(Long orderId, Order order) {
        order.setOrderId(10000L);
        return false;
    }

    @Override
    @LogRecordAnnotation(success = "更新了订单{IDENTITY{#order.orderId}},更新内容为...{IDENTITY{#order.orderNo}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            detail = "{{#order.toString()}}")
    public boolean identity(Long orderId, Order order) {
        return false;
    }

    @LogRecordAnnotation(success = "更新了订单{_DIFF{#oldOrder, #newOrder}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            detail = "{{#newOrder.toString()}}")
    public boolean diff(Order oldOrder, Order newOrder) {

        return false;
    }

    @LogRecordAnnotation(success = "更新了订单{_DIFF{#newOrder}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            detail = "{{#newOrder.toString()}}")
    @Override
    public boolean diff1(Order newOrder) {

        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, null);
        return false;
    }

    @Override
    @LogRecordAnnotation(success = "更新了订单{_DIFF{#newOrder}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            detail = "{{#newOrder.toString()}}")
    public boolean diff2(Order newOrder) {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000011");
        order.setProductName("超值优惠红烧肉套餐");
        order.setPurchaseName("张三");
        Order.UserDO userDO = new Order.UserDO();
        userDO.setUserId(9001L);
        userDO.setUserName("用户1");
        order.setCreator(userDO);
        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, order);

        return false;
    }

    @Override
    @LogRecordAnnotation(success = "更新了订单{ORDER{#orderId}},更新内容为...",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            condition = "{{#condition == null}}")
    public boolean testCondition(Long orderId, Order order, String condition) {
        return false;
    }

    @Override
    @LogRecordAnnotation(success = "更新了订单{ORDER{#orderId}},更新内容为..{{#title}}",
            prefix = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean testContextCallContext(Long orderId, Order order) {
        LogRecordContext.putVariable("title", "外层调用");
        userQueryService.getUserList(Lists.newArrayList("mzt"));
        return false;
    }
}
