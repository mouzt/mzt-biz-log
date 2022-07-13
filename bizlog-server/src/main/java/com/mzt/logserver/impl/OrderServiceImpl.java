package com.mzt.logserver.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.IOrderService;
import com.mzt.logserver.UserQueryService;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @LogRecord(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            subType = "MANAGER_VIEW",
            extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}", actionType = "INSERT")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }

    @Override
    @LogRecord(fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            extra = "{{#orders}}",
            success = "{{#purchaseNameList}}下了一个订单,购买商品「{{#productNameList}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#orderNoList}}",
            isBatch = true, actionType = "INSERT")
    public boolean createBatchOrder(List<Order> orders) {
        Optional.ofNullable(orders).ifPresent(x -> {
            x.forEach(y -> log.info("【创建订单】orderNo={}", y.getOrderNo()));
            List<String> purchaseNameList = orders.stream()
                    .map(Order::getPurchaseName).collect(Collectors.toList());
            List<String> productNameList = orders.stream()
                    .map(Order::getProductName).collect(Collectors.toList());
            List<String> orderNoList = orders.stream()
                    .map(Order::getOrderNo).collect(Collectors.toList());
            LogRecordContext.putVariable("purchaseNameList", purchaseNameList);
            LogRecordContext.putVariable("productNameList", productNameList);
            LogRecordContext.putVariable("orderNoList", orderNoList);
        });
        return true;
    }

    @Override
    @LogRecord(fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            extra = "{{#orders}}",
            success = "{{#purchaseNameList}}下了一个订单,购买商品「{{#productNameList}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#orderNoList}}",
            isBatch = true)
    public boolean createBatchOrder1(List<Order> orders) {
        Optional.ofNullable(orders).ifPresent(x -> {
            List<String> purchaseNameList = x.stream()
                    .map(Order::getPurchaseName).collect(Collectors.toList());
            List<String> productNameList = x.stream()
                    .map(Order::getProductName).collect(Collectors.toList());
            List<String> orderNoList = x.stream()
                    .map(Order::getOrderNo).collect(Collectors.toList());
            // bizNo的length更大的情况
            // case1
//            orderNoList.add("MT0000013");
            //   case2
            purchaseNameList.remove("李四");
            // bizNo的length更小的情况
//            purchaseNameList.add("王五");
//            productNameList.add("超值优惠香酥鸭套餐");
            LogRecordContext.putVariable("purchaseNameList", purchaseNameList);
            LogRecordContext.putVariable("productNameList", productNameList);
            LogRecordContext.putVariable("orderNoList", orderNoList);
        });
        return true;
    }

    @Override
    @LogRecord(fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            extra = "{{#orders}}",
            success = "{{#purchaseNameList}}下了一个订单,购买商品「{{#productNameList}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#orderNoList}}",
            isBatch = true)
    public boolean createBatchOrder2(List<Order> orders) {
        Optional.ofNullable(orders).ifPresent(x -> {
            if (CollectionUtil.isNotEmpty(x)) {
                LogRecordContext.putVariable("purchaseNameList", x.get(0).getPurchaseName());
                LogRecordContext.putVariable("productNameList", x.get(0).getProductName());
            }
            List<String> orderNoList = x.stream()
                    .map(Order::getOrderNo).collect(Collectors.toList());
            LogRecordContext.putVariable("orderNoList", orderNoList);
        });
        return true;
    }

    @Override
    @LogRecord(fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            extra = "{{#order}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#productNameList}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            isBatch = true)
    public boolean createBatchOrder3(Order order) {
        Optional.ofNullable(order).ifPresent(x -> {
            List<String> productNameList = CollectionUtil.newArrayList(x.getProductName(), "超值优惠香酥鸭套餐");
            LogRecordContext.putVariable("productNameList", productNameList);
        });
        return true;
    }

    @Override
    public boolean createOrder_interface(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }


    /*'张三下了一个订单,购买商品「超值优惠红烧肉套餐」,下单结果:true' */
    @Override
    @LogRecord(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            subType = "MANAGER_VIEW",
            extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
//            isBatch = true,
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
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
    @LogRecord(success = "更新了订单{ORDER_BEFORE{#order.orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
//            isBatch = true,
            extra = "{{#order.toString()}}")
    public boolean updateBefore(Long orderId, Order order) {
        order.setOrderId(10000L);
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#order.orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            extra = "{{#order.toString()}}")
    public boolean updateAfter(Long orderId, Order order) {
        order.setOrderId(10000L);
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{IDENTITY{#order.orderId}},更新内容为...{IDENTITY{#order.orderNo}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            extra = "{{#order.toString()}}")
    public boolean identity(Long orderId, Order order) {
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#oldOrder, #newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            extra = "{{#newOrder.toString()}}")
    public boolean diff(Order oldOrder, Order newOrder) {

        return false;
    }

    @Override
    @LogRecord(success = "测试刀了符号{DOLLAR{#order.orderId}}哈哈哈",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            extra = "{{#order.toString()}}")
    public boolean dollar(Long orderId, Order order) {
        return false;
    }

    @LogRecord(success = "更新了订单{_DIFF{#newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            extra = "{{#newOrder.toString()}}")
    @Override
    public boolean diff1(Order newOrder) {

        LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, null);
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#newOrder.orderNo}}",
            extra = "{{#newOrder.toString()}}")
    public boolean diff2(Order newOrder) {
        Order order = new Order();
        order.setOrderId(99L);
        order.setOrderNo("MT0000099");
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
    @LogRecord(success = "更新了订单{_DIFF{#oldOrders, #newOrders}}",
            type = LogRecordType.ORDER, bizNo = "{{#orderNos}}",
            extra = "{{#newOrders}}", isBatch = true)
    public boolean diff3(List<Order> oldOrders, List<Order> newOrders) {
        Optional.ofNullable(newOrders).ifPresent(x -> LogRecordContext.putVariable("orderNos",
                x.stream().map(Order::getOrderNo).collect(Collectors.toList())));
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#oldOrder, #newOrders}}",
            type = LogRecordType.ORDER, bizNo = "{{#id}}",
            extra = "{{#newOrders}}", isBatch = true)
    public boolean diff4(Order oldOrder, List<Order> newOrders) {
        Optional.ofNullable(newOrders).ifPresent(x -> {
            List<String> id = x.stream().map(Order::getOrderNo).collect(Collectors.toList());
//            id.remove("MT0000099");
            LogRecordContext.putVariable("id", id);
        });
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#oldOrders, #newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#id}}",
            extra = "{{#newOrder}}", isBatch = true)
    public boolean diff5(List<Order> oldOrders, Order newOrder) {
        Optional.ofNullable(newOrder).ifPresent(x -> {
            List<String> id = new ArrayList<>(16);
            id.add(x.getOrderNo());
//            id.add("MT0000100");
//            LogRecordContext.putVariable("id", newOrder.getOrderNo());
            LogRecordContext.putVariable("id", id);
        });
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#oldOrder, #newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#id}}",
            extra = "{{#newOrder.toString()}}", isBatch = true)
    public boolean diff6(Order oldOrder, Order newOrder) {
        Optional.ofNullable(newOrder).ifPresent(x -> {
            List<String> id = new ArrayList<>(16);
            id.add(x.getOrderNo());
            id.add("MT0000100");
//            LogRecordContext.putVariable("id", newOrder.getOrderNo());
            LogRecordContext.putVariable("id", id);
        });
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#newOrders}}",
            type = LogRecordType.ORDER, bizNo = "{{#id}}",
            extra = "{{#newOrders}}", isBatch = true)
    public boolean diff7(List<Order> newOrders) {
        Optional.ofNullable(newOrders).ifPresent(x -> {
            Order orderA = new Order();
            orderA.setOrderId(99L);
            orderA.setOrderNo("MT0000099");
            orderA.setProductName("超值优惠红烧肉套餐");
            orderA.setPurchaseName("张三");
            Order.UserDO userDO = new Order.UserDO();
            userDO.setUserId(9001L);
            userDO.setUserName("用户1");
            orderA.setCreator(userDO);
            orderA.setItems(Lists.newArrayList("123", "bbb"));
            Order orderB = new Order();
            orderB.setOrderId(88L);
            orderB.setOrderNo("MT0000099");
            orderB.setProductName("麻辣烫套餐");
            orderB.setPurchaseName("赵四");
            Order.UserDO userDO1 = new Order.UserDO();
            userDO1.setUserId(9002L);
            userDO1.setUserName("用户2");
            orderB.setCreator(userDO1);
            orderB.setItems(Lists.newArrayList("123", "aaa"));
            List<Order> oldList = CollectionUtil.newArrayList(orderA, orderB);

            List<String> id = x.stream().map(Order::getOrderNo).collect(Collectors.toList());
//            id.add("MT0000100");
            LogRecordContext.putVariable("id", id);
            LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldList);
//            LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, orderA);
        });
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{_DIFF{#newOrder}}",
            type = LogRecordType.ORDER, bizNo = "{{#id}}",
            extra = "{{#newOrder}}", isBatch = true)
    public boolean diff8(Order newOrder) {
        Optional.ofNullable(newOrder).ifPresent(x -> {
            Order orderA = new Order();
            orderA.setOrderId(99L);
            orderA.setOrderNo("MT0000099");
            orderA.setProductName("超值优惠红烧肉套餐");
            orderA.setPurchaseName("张三");
            Order.UserDO userDO = new Order.UserDO();
            userDO.setUserId(9001L);
            userDO.setUserName("用户1");
            orderA.setCreator(userDO);
            orderA.setItems(Lists.newArrayList("123", "bbb"));
            Order orderB = new Order();
            orderB.setOrderId(88L);
            orderB.setOrderNo("MT0000099");
            orderB.setProductName("麻辣烫套餐");
            orderB.setPurchaseName("赵四");
            Order.UserDO userDO1 = new Order.UserDO();
            userDO1.setUserId(9002L);
            userDO1.setUserName("用户2");
            orderB.setCreator(userDO1);
            orderB.setItems(Lists.newArrayList("123", "aaa"));
            List<Order> oldList = CollectionUtil.newArrayList(orderA, orderB);

            List<String> id = new ArrayList<>(16);
            id.add(x.getOrderNo());
//            id.add("MT0000100");
//            LogRecordContext.putVariable("id", x.getOrderNo());
            LogRecordContext.putVariable("id", id);
            LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, oldList);
//            LogRecordContext.putVariable(DiffParseFunction.OLD_OBJECT, orderA);
        });
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
//            isBatch = true,
            condition = "{{#condition == null}}")
    public boolean testCondition(Long orderId, Order order, String condition) {
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为..{{#title}}",
//            isBatch = true,
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean testContextCallContext(Long orderId, Order order) {
        LogRecordContext.putVariable("title", "外层调用");
        userQueryService.getUserList(Lists.newArrayList("mzt"));
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为..{{#title}}",
//            isBatch = true,
            type = LogRecordType.ORDER, subType = "{{#order.orderNo}}", bizNo = "{{#order.orderNo}}")
    public boolean testSubTypeSpEl(Long orderId, Order order) {
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为..{{#title}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean testVariableInfo(Long orderId, Order order) {
        return false;
    }
}
