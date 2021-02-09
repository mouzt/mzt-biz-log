package com.mzt.logserver.function;

import com.mzt.logapi.service.IParseFunction;
import org.springframework.stereotype.Component;

/**
 * @author muzhantong
 * create on 2021/2/9 5:20 下午
 */
@Component
public class OrderParseFunction implements IParseFunction {
    @Override
    public String functionName() {
        return "ORDER";
    }

    @Override
    public String apply(String value) {
        return "小明".concat("(").concat(value).concat(")");
    }
}
