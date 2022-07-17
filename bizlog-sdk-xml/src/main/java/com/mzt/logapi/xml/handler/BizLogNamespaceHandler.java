package com.mzt.logapi.xml.handler;

import com.mzt.logapi.xml.parser.BizLogBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author muzhantong
 * create on 2022/5/6 9:05 PM
 */
public class BizLogNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        registerBeanDefinitionParser("log-record", new BizLogBeanDefinitionParser());
    }
}
