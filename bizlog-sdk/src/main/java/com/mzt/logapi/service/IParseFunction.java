package com.mzt.logapi.service;

public interface IParseFunction {

    default boolean executeBefore(){
        return true;
    }
    String functionName();

    String apply(String value);
}
