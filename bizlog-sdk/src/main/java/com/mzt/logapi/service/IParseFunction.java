package com.mzt.logapi.service;

public interface IParseFunction {

    default boolean executeBefore(){
        return false;
    }

    String functionName();

    String apply(Object value);
}
