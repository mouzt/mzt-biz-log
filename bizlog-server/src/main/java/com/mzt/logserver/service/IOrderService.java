package com.mzt.logserver.service;

import com.mzt.logserver.beans.Order;

/**
 * @author muzhantong
 * create on 2020/6/12 11:07 上午
 */
public interface IOrderService {
     boolean createOrder(Order order);
}
