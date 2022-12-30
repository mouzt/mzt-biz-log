package com.mzt.logserver;

import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.Order;
import com.mzt.logserver.pojo.Result;
import com.mzt.logserver.pojo.User;

/**
 * @author muzhantong
 * create on 2020/6/12 11:07 上午
 */
public interface IOrderService {
    @LogRecord(
            fail = "创建订单失败，失败原因：「{{#_errorMsg}}」",
            subType = "MANAGER_VIEW",
            extra = "{{#order.toString()}}",
            success = "{{#order.purchaseName}}下了一个订单,购买商品「{{#order.productName}}」,测试变量「{{#innerOrder.productName}}」,下单结果:{{#_ret}}",
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    boolean createOrder_interface(Order order);

    boolean createOrder(Order order);

    boolean createOrders(Order order);

    boolean createOrder_fail(Order order);

    boolean updateBefore(Long orderId, Order order);

    boolean updateAfter(Long orderId, Order order);

    boolean identity(Long orderId, Order order);

    boolean dollar(Long orderId, Order order);

    boolean diff(Order oldOrder, Order newOrder);

    boolean diff1(Order newOrder);

    boolean diff2(Order newOrder);

    boolean testCondition(Long orderId, Order order, String condition);

    boolean testContextCallContext(Long orderId, Order order);

    boolean testSubTypeSpEl(Long orderId, Order order);

    boolean testVariableInfo(Long orderId, Order order);

    Result<Boolean> testResultOnSuccess(Long orderId, Order order);

    Result<Boolean> testResultOnFail(Long orderId, Order order);

    Result<Boolean> testResultNoLog(Long orderId, Order order);

    boolean testGlobalVariable(Order order);

    boolean testGlobalVariableCover(Order order, User user);

    void fixedCopy(String text);

    void fixedCopy2(User user, User oldUser);
}
