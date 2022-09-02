package com.mzt.logserver.function;

import com.mzt.logapi.service.IParseFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * @author wulang
 **/
@Slf4j
@Component
public class ExtInfoParseFunction implements IParseFunction {
    @Override
    public boolean executeBefore() {
        return false;
    }

    @Override
    public String functionName() {
        return "extInfo";
    }

    @Override
    public String apply(Object value) {
        log.info("===========");
        if (StringUtils.isEmpty(value)) {
            return "";
        }
        log.info("当前拓展信息值为,{}", value);
        return value.toString();
    }
}
