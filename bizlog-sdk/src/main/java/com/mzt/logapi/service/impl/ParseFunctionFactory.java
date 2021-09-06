package com.mzt.logapi.service.impl;

import com.mzt.logapi.service.IParseFunction;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author muzhantong
 * create on 2021/2/6 9:45 上午
 */

public class ParseFunctionFactory {
    private Map<String, IParseFunction> afterExecuteFunctionMap;
    private Map<String, IParseFunction> beforeExecuteFunctionMap;
    private Map<String, IParseFunction> allFunctionMap;

    public ParseFunctionFactory(List<IParseFunction> parseFunctions) {
        if (CollectionUtils.isEmpty(parseFunctions)) {
            return;
        }
        afterExecuteFunctionMap = new HashMap<>();
        beforeExecuteFunctionMap = new HashMap<>();
        allFunctionMap = new HashMap<>();
        for (IParseFunction parseFunction : parseFunctions) {
            if (StringUtils.isEmpty(parseFunction.functionName())) {
                continue;
            }
            if(parseFunction.executeBefore()){
                beforeExecuteFunctionMap.put(parseFunction.functionName(), parseFunction);
            }else {
                afterExecuteFunctionMap.put(parseFunction.functionName(), parseFunction);
            }
            allFunctionMap.put(parseFunction.functionName(), parseFunction);
        }
    }

    public IParseFunction getAfterFunction(String functionName) {
        return afterExecuteFunctionMap.get(functionName);
    }
    public IParseFunction getBeforeFunction(String functionName) {
        return beforeExecuteFunctionMap.get(functionName);
    }
    public IParseFunction getFunction(String functionName){
        return allFunctionMap.get(functionName);
    }
}
