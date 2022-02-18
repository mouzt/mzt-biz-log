package com.mzt.logserver.function;

import com.mzt.logapi.service.IParseFunction;
import org.springframework.stereotype.Component;

/**
 * @author muzhantong
 * create on 2022/2/17 4:56 PM
 */
@Component
public class DollarParseFunction implements IParseFunction {
    @Override
    public String functionName() {
        return "DOLLAR";
    }

    @Override
    public String apply(Object value) {
        return "10$,/666";
    }
}
