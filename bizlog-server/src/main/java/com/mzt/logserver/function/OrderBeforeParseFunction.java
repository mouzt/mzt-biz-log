package com.mzt.logserver.function;

import com.mzt.logapi.service.IParseFunction;
import com.mzt.logserver.UserQueryService;
import com.mzt.logserver.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * @author muzhantong
 * create on 2021/2/9 5:20 下午
 */
@Slf4j
@Component
public class OrderBeforeParseFunction implements IParseFunction {

    @Resource
//    @Lazy
    private UserQueryService userQueryService;

    @Override
    public boolean executeBefore() {
        return true;
    }

    @Override
    public String functionName() {
        return "ORDER_BEFORE";
    }

    @Override
    public String apply(Object value) {
        log.info("@@@@@@@@");
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        log.info("###########,{}", value);
        Order order = new Order();
        order.setProductName("xxxx");
        return order.getProductName().concat("(").concat(value.toString()).concat(")");
    }
}
