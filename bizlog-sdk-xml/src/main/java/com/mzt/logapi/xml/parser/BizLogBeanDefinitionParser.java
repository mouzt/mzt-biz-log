package com.mzt.logapi.xml.parser;

import com.mzt.logapi.service.impl.*;
import com.mzt.logapi.starter.configuration.LogRecordProperties;
import com.mzt.logapi.starter.diff.DefaultDiffItemsToLogContentService;
import com.mzt.logapi.starter.support.aop.BeanFactoryLogRecordAdvisor;
import com.mzt.logapi.starter.support.aop.LogRecordInterceptor;
import com.mzt.logapi.starter.support.aop.LogRecordOperationSource;
import com.mzt.logapi.starter.support.parse.LogFunctionParser;
import org.springframework.aop.config.AopNamespaceUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * @author muzhantong
 * create on 2022/5/6 8:49 PM
 */
public class BizLogBeanDefinitionParser implements BeanDefinitionParser {
    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        String tenant = element.getAttribute("tenant");
        AopNamespaceUtils.registerAutoProxyCreatorIfNecessary(parserContext, element);


        Object eleSource = parserContext.extractSource(element);
        RootBeanDefinition logRecordOperationSource = new RootBeanDefinition(LogRecordOperationSource.class);
        logRecordOperationSource.setSource(eleSource);
        logRecordOperationSource.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);


        RootBeanDefinition operateGetServiceDef = new RootBeanDefinition(DefaultOperatorGetServiceImpl.class);
        operateGetServiceDef.setSource(eleSource);
        operateGetServiceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        parserContext.getRegistry().registerBeanDefinition("operateGetService", operateGetServiceDef);


        RootBeanDefinition parseFunctionDef = new RootBeanDefinition(DefaultParseFunction.class);
        parseFunctionDef.setSource(eleSource);
        parseFunctionDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        RootBeanDefinition parseFunctionFactoryDef = new RootBeanDefinition(ParseFunctionFactory.class);
        parseFunctionFactoryDef.getConstructorArgumentValues().addGenericArgumentValue(parseFunctionDef);
        parseFunctionFactoryDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        RootBeanDefinition functionServiceDef = new RootBeanDefinition(DefaultFunctionServiceImpl.class);
        functionServiceDef.setSource(eleSource);
        functionServiceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        functionServiceDef.getConstructorArgumentValues().addGenericArgumentValue(parseFunctionFactoryDef);

        RootBeanDefinition logFunctionParserDef = new RootBeanDefinition(LogFunctionParser.class);
        logFunctionParserDef.setSource(eleSource);
        logFunctionParserDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        logFunctionParserDef.getConstructorArgumentValues().addGenericArgumentValue(functionServiceDef);

        RootBeanDefinition diffParseFunctionDef = new RootBeanDefinition(DiffParseFunction.class);
        diffParseFunctionDef.setSource(eleSource);
        diffParseFunctionDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        RootBeanDefinition logRecordPropertiesDef = new RootBeanDefinition(LogRecordProperties.class);
        logRecordPropertiesDef.setSource(eleSource);
        logRecordPropertiesDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);


        RootBeanDefinition logRecordServiceDef = new RootBeanDefinition(DefaultLogRecordServiceImpl.class);
        logRecordServiceDef.setSource(eleSource);
        logRecordServiceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        parserContext.getRegistry().registerBeanDefinition("logRecordService", logRecordServiceDef);

        RootBeanDefinition diffItemsToLogContentServiceDef = new RootBeanDefinition(DefaultDiffItemsToLogContentService.class);
        diffItemsToLogContentServiceDef.setSource(eleSource);
        diffItemsToLogContentServiceDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);


        RootBeanDefinition logRecordInterceptorDef = new RootBeanDefinition(LogRecordInterceptor.class);
        logRecordInterceptorDef.setSource(eleSource);
        logRecordInterceptorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);

        logRecordInterceptorDef.getPropertyValues().addPropertyValue("logRecordOperationSource", logRecordOperationSource);
        logRecordInterceptorDef.getPropertyValues().addPropertyValue("logFunctionParser", logFunctionParserDef);
        logRecordInterceptorDef.getPropertyValues().addPropertyValue("diffParseFunction", diffParseFunctionDef);
        logRecordInterceptorDef.getPropertyValues().addPropertyValue("tenant", tenant);
        logRecordInterceptorDef.getPropertyValues().addPropertyValue("logRecordService", logRecordServiceDef);


        RootBeanDefinition logRecordAdvisorDef = new RootBeanDefinition(BeanFactoryLogRecordAdvisor.class);
        logRecordAdvisorDef.setSource(eleSource);
        logRecordAdvisorDef.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        logRecordAdvisorDef.getPropertyValues().add("logRecordOperationSource", logRecordOperationSource);
        logRecordAdvisorDef.getPropertyValues().add("advice", logRecordInterceptorDef);

        parserContext.getRegistry().registerBeanDefinition("logRecordAdvisor", logRecordAdvisorDef);


        CompositeComponentDefinition compositeDef = new CompositeComponentDefinition(element.getTagName(), eleSource);
        compositeDef.addNestedComponent(new BeanComponentDefinition(logRecordOperationSource, "logRecordOperationSource"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(operateGetServiceDef, "operateGetService"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(parseFunctionDef, "parseFunction"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(parseFunctionFactoryDef, "parseFunctionFactory"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(functionServiceDef, "functionService"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(logFunctionParserDef, "logFunctionParser"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(diffParseFunctionDef, "diffParseFunction"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(logRecordPropertiesDef, "logRecordProperties"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(logRecordServiceDef, "logRecordService"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(diffItemsToLogContentServiceDef, "diffItemsToLogContentService"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(logRecordInterceptorDef, "logRecordInterceptor"));
        compositeDef.addNestedComponent(new BeanComponentDefinition(logRecordAdvisorDef, "logRecordAdvisor"));
        parserContext.registerComponent(compositeDef);

        return null;
    }
}
