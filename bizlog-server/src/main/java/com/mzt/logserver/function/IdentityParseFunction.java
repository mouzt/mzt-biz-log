package com.mzt.logserver.function;

import com.mzt.logapi.service.IParseFunction;

/**
 * @author muzhantong
 * create on 2022/1/3 2:43 下午
 */
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
    public String apply(String value) {
        return value;
    }
}
