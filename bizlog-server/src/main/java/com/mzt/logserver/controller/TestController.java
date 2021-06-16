package com.mzt.logserver.controller;

import com.mzt.logserver.context.TenantContext;
import com.mzt.logserver.context.TenantEnum;
import com.mzt.logserver.mapper.OrderMapper;
import com.mzt.logserver.po.Order;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class TestController {
    @Resource
    private OrderMapper orderMapper;

    @RequestMapping("/test1")
    public String test1(){
        TenantContext.setTenant(TenantEnum.TEST1.name());
        Order order = orderMapper.selectByPrimaryKey(1L);
        return order.getProductNo();
    }

    @RequestMapping("/test2")
    public String test2(){
        TenantContext.setTenant(TenantEnum.TEST2.name());
        Order order = orderMapper.selectByPrimaryKey(1L);
        return order.getProductNo();
    }
}
