package com.mzt.logserver.infrastructure.logrecord.function;


import com.mzt.logapi.service.IParseFunction;
import org.springframework.stereotype.Component;

/**
 * @author muzhantong
 * create on 2022/1/3 2:43 下午
 */
@Component
public class IdentityParseFunction implements IParseFunction {

    @Override
    public boolean executeBefore() {
        return true;
    }

    @Override
    public String functionName() {
        return "IDENTITY";
    }

    @Override
    public String apply(Object value) {
        return value.toString();
    }
}
