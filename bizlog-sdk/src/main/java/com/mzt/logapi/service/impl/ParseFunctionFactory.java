package com.mzt.logapi.service.impl;

import com.google.common.collect.Maps;
import com.mzt.logapi.service.IParseFunction;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;


/**
 * @author muzhantong
 * create on 2021/2/6 9:45 上午
 */

public class ParseFunctionFactory {
    private Map<String, IParseFunction> functionMap;

    public ParseFunctionFactory(List<IParseFunction> parseFunctions) {
        functionMap = Maps.newHashMap();
        if (CollectionUtils.isEmpty(parseFunctions)) {
            return;
        }
        for (IParseFunction parseFunction : parseFunctions) {
            if (StringUtils.isEmpty(parseFunction.functionName())) {
                continue;
            }
            functionMap.put(parseFunction.functionName(), parseFunction);
        }
    }

    public IParseFunction getFunction(String functionName) {
        return functionMap.get(functionName);
    }
}
