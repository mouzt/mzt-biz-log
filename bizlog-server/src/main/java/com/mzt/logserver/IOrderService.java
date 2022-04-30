package com.mzt.logserver;

import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.pojo.Order;

import java.util.List;

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
//            isBatch = true,
            type = LogRecordType.ORDER, bizNo = "{{#order.orderNo}}")
    boolean createOrder_interface(Order order);

    boolean createOrder(Order order);

    /**
     * <p>批量创建订单</p>
     * @param orders 订单列表
     * @return 是否创建成功
     * */
    boolean createBatchOrder(List<Order> orders);

    /**
     * <p>批量创建订单</p>
     * <p>测试 {@link LogRecord#bizNo()} 里的expression和
     *  {@link LogRecord} 其它属性里的expression同时为 {@link List} 时
     * 且长度不一致的情况下，批量保存日志是否运行正常</p>
     * @param orders 订单列表
     * @return 是否创建成功
     * */
    boolean createBatchOrder1(List<Order> orders);

    /**
     * <p>批量创建订单</p>
     * <p>测试 {@link LogRecord#bizNo()} 里的expression为 {@link List} ，而
     *  {@link LogRecord} 其它属性里的expression不同时为 {@link List} 时，
     * 批量保存日志是否运行正常</p>
     * @param orders 订单列表
     * @return 是否创建成功
     * */
    boolean createBatchOrder2(List<Order> orders);

    /**
     * <p>批量创建订单</p>
     * <p>测试 {@link LogRecord#bizNo()} 里的expression不为 {@link List} ，而
     *  {@link LogRecord} 其它属性里的expression不同时为 {@link List} 时，
     * 批量保存日志是否运行正常</p>
     * @param order 订单
     * @return 是否创建成功
     * */
    boolean createBatchOrder3(Order order);

    boolean createOrder_fail(Order order);

    boolean updateBefore(Long orderId, Order order);

    boolean updateAfter(Long orderId, Order order);

    boolean identity(Long orderId, Order order);

    boolean dollar(Long orderId, Order order);

    boolean diff(Order oldOrder, Order newOrder);

    boolean diff1(Order newOrder);

    boolean diff2(Order newOrder);

    /**
     * <p>适用于需要同时使用diff和批量保存日志的功能</p>
     * @param oldOrders 旧的订单列表
     * @param newOrders 新的订单列表
     * @return {@code true} or {@code false}
     * */
    boolean diff3(List<Order> oldOrders, List<Order> newOrders);

    /**
     * <p>适用于需要同时使用diff和批量保存日志的功能</p>
     * @param oldOrder 旧的订单
     * @param newOrders 新的订单列表
     * @return {@code true} or {@code false}
     * */
    boolean diff4(Order oldOrder, List<Order> newOrders);

    /**
     * <p>适用于需要同时使用diff和批量保存日志的功能</p>
     * @param oldOrders 旧的订单列表
     * @param newOrder 新的订单
     * @return {@code true} or {@code false}
     * */
    boolean diff5(List<Order> oldOrders, Order newOrder);

    /**
     * <p>适用于需要同时使用diff和批量保存日志的功能</p>
     * @param oldOrder 旧的订单
     * @param newOrder 新的订单
     * @return {@code true} or {@code false}
     * */
    boolean diff6(Order oldOrder, Order newOrder);


    /**
     * <p>适用于需要同时使用diff和批量保存日志的功能</p>
     * @param newOrders 新的订单列表
     * @return {@code true} or {@code false}
     * */
    boolean diff7(List<Order> newOrders);

    /**
     * <p>适用于需要同时使用diff和批量保存日志的功能</p>
     * @param newOrder 新的订单
     * @return {@code true} or {@code false}
     * */
    boolean diff8(Order newOrder);

    boolean testCondition(Long orderId, Order order, String condition);

    boolean testContextCallContext(Long orderId, Order order);

    boolean testSubTypeSpEl(Long orderId, Order order);

    boolean testVariableInfo(Long orderId, Order order);
}
