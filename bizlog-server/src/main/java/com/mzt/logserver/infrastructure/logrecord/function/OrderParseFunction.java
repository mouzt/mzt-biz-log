package com.mzt.logserver.infrastructure.logrecord.function;

import com.mzt.logapi.service.IParseFunction;
import com.mzt.logserver.pojo.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author muzhantong
 * create on 2021/2/9 5:20 下午
 */
@Slf4j
@Component
public class OrderParseFunction implements IParseFunction {

    @Override
    public boolean executeBefore() {
        return false;
    }

    @Override
    public String functionName() {
        return "ORDER";
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
