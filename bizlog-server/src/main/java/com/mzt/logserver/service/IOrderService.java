package com.mzt.logserver.service;

import com.mzt.logserver.beans.Order;

/**
 * @author muzhantong
 * create on 2020/6/12 11:07 上午
 */
public interface IOrderService {
    boolean createOrder(Order order);

    boolean createOrder_fail(Order order);

    boolean update(Long orderId, Order order);

    boolean identity(Long orderId, Order order);

    boolean diff(Order oldOrder, Order newOrder);

    boolean diff1(Order newOrder);

    boolean diff2(Order newOrder);

    boolean testCondition(Long orderId, Order order, String condition);

    boolean testContextCallContext(Long orderId, Order order);
}
