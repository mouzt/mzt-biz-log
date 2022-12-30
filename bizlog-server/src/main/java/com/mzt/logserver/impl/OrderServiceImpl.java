package com.mzt.logserver.impl;

import com.mzt.logapi.context.LogRecordContext;
import com.mzt.logapi.service.impl.DiffParseFunction;
import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.IOrderService;
import com.mzt.logserver.UserQueryService;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.Order;
import com.mzt.logserver.pojo.Result;
import com.mzt.logserver.pojo.User;
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
    @LogRecord(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            subType = "MANAGER_VIEW",
            extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean createOrder(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
        // db insert order
        Order order1 = new Order();
        order1.setProductName("内部变量测试");
        LogRecordContext.putVariable("innerOrder", order1);
        return true;
    }

    @Override
    @LogRecord(
            subType = "MANAGER_VIEW", extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    @LogRecord(
            subType = "USER_VIEW",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.USER, bizNo = "{{#order.orderNo}}")
    public boolean createOrders(Order order) {
        log.info("【创建订单】orderNo={}", order.getOrderNo());
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
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            condition = "{{#condition == null}}")
    public boolean testCondition(Long orderId, Order order, String condition) {
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为..{{#title}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    public boolean testContextCallContext(Long orderId, Order order) {
        LogRecordContext.putVariable("title", "外层调用");
        userQueryService.getUserList(Lists.newArrayList("mzt"));
        return false;
    }

    @Override
    @LogRecord(success = "更新了订单{ORDER{#orderId}},更新内容为..{{#title}}",
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

    @Override
    @LogRecord(success = "更新成功了订单{ORDER{#orderId}},更新内容为...",
            fail = "更新失败了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            condition = "{{#condition == null}}", successCondition = "{{#result.code == 200}}")
    public Result<Boolean> testResultOnSuccess(Long orderId, Order order) {
        Result<Boolean> result = new Result<>(200, "成功", true);
        LogRecordContext.putVariable("result", result);
        return result;
    }

    @Override
    @LogRecord(success = "更新成功了订单{ORDER{#orderId}},更新内容为...",
            fail = "更新失败了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            condition = "{{#condition == null}}", successCondition = "{{#result.code == 200}}")
    public Result<Boolean> testResultOnFail(Long orderId, Order order) {
        Result<Boolean> result = new Result<>(500, "服务错误", false);
        LogRecordContext.putVariable("result", result);
        return result;
    }

    @Override
    @LogRecord(success = "更新成功了订单{ORDER{#orderId}},更新内容为...",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}",
            condition = "{{#condition == null}}", successCondition = "{{#result.code == 200}}")
    public Result<Boolean> testResultNoLog(Long orderId, Order order) {
        Result<Boolean> result = new Result<>(500, "服务错误", false);
        LogRecordContext.putVariable("result", result);
        return result;
    }

    @Override
    @LogRecord(success = "更新用户{{#user.name}}的订单{ORDER{#order.orderId}}信息,更新内容为...",
            type = LogRecordType.USER, bizNo = "{{#user.id}}")
    public boolean testGlobalVariable(Order order) {
        return false;
    }

    @Override
    @LogRecord(success = "更新用户{{#user.name}}的订单{ORDER{#order.orderId}}信息,更新内容为...",
            type = LogRecordType.USER, bizNo = "{{#user.id}}")
    public boolean testGlobalVariableCover(Order order, User user) {
        return false;
    }

    @Override
    @LogRecord(success = "固定文案记录日志",
            type = LogRecordType.USER, bizNo = "{{#text}}")
    public void fixedCopy(String text) {
        log.info("进入方法。。。");
    }

    @Override
    @LogRecord(success = "更新了用户{_DIFF{#user, #oldUser}}",
            type = LogRecordType.USER, bizNo = "{{#user.name}}",
            extra = "{{#user.toString()}}")
    public void fixedCopy2(User user, User oldUser) {

    }
}
