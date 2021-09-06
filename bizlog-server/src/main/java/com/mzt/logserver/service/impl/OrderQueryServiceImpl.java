package com.mzt.logserver.service.impl;

import com.mzt.logserver.beans.Order;
import com.mzt.logserver.service.OrderQueryService;
import org.springframework.stereotype.Service;

/**
 * @author muzhantong
 * create on 2021/2/10 11:14 上午
 */
@Service
public class OrderQueryServiceImpl implements OrderQueryService {
    @Override
    public Order queryOrder(long parseLong) {
        Order order = new Order();
        order.setProductName("xxxx");
        return order;
    }
}
