package com.mzt.logapi.service;

public interface IParseFunction {
    String functionName();

    String apply(String value);
}
