package com.mzt.logapi.starter.configuration;

import com.mzt.logapi.service.IFunctionService;
import com.mzt.logapi.service.ILogRecordService;
import com.mzt.logapi.service.IOperatorGetService;
import com.mzt.logapi.service.IParseFunction;
import com.mzt.logapi.service.impl.*;
import com.mzt.logapi.starter.annotation.EnableLogRecord;
import com.mzt.logapi.starter.support.aop.BeanFactoryLogRecordAdvisor;
import com.mzt.logapi.starter.support.aop.LogRecordInterceptor;
import com.mzt.logapi.starter.support.aop.LogRecordOperationSource;
import com.mzt.logapi.starter.support.diff.DefaultDiffItemsToLogContentService;
import com.mzt.logapi.starter.support.diff.IDiffItemsToLogContentService;
import com.mzt.logapi.starter.support.diff.ObjectDiffUtil;
import com.mzt.logapi.starter.support.parse.LogFunctionParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Role;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

/**
 * @author muzhantong
 * create on 2020/6/12 10:41 上午
 */
@Configuration
@Slf4j
public class LogRecordProxyAutoConfiguration implements ImportAware, BeanPostProcessor {

    private AnnotationAttributes enableLogRecord;


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordOperationSource logRecordOperationSource() {
        return new LogRecordOperationSource();
    }

    @Bean
    @ConditionalOnMissingBean(IFunctionService.class)
    public IFunctionService functionService(ParseFunctionFactory parseFunctionFactory) {
        return new DefaultFunctionServiceImpl(parseFunctionFactory);
    }

    @Bean
    public ParseFunctionFactory parseFunctionFactory(@Autowired List<IParseFunction> parseFunctions) {
        return new ParseFunctionFactory(parseFunctions);
    }

    @Bean
    @ConditionalOnMissingBean(IParseFunction.class)
    public DefaultParseFunction parseFunction() {
        return new DefaultParseFunction();
    }


    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLogRecordAdvisor logRecordAdvisor(LogFunctionParser logFunctionParser) {
        BeanFactoryLogRecordAdvisor advisor =
                new BeanFactoryLogRecordAdvisor();
        advisor.setLogRecordOperationSource(logRecordOperationSource());
        advisor.setAdvice(logRecordInterceptor(logFunctionParser));
        return advisor;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordInterceptor logRecordInterceptor(LogFunctionParser logFunctionParser) {
        LogRecordInterceptor interceptor = new LogRecordInterceptor();
        interceptor.setLogRecordOperationSource(logRecordOperationSource());
        interceptor.setTenant(enableLogRecord.getString("tenant"));
        interceptor.setLogFunctionParser(logFunctionParser);
        return interceptor;
    }

    @Bean
    public LogFunctionParser logFunctionParser(IFunctionService functionService) {
        LogFunctionParser logFunctionParser = new LogFunctionParser();
        logFunctionParser.setFunctionService(functionService);
        return logFunctionParser;
    }

    @Bean
    @ConditionalOnMissingBean(IOperatorGetService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IOperatorGetService operatorGetService() {
        return new DefaultOperatorGetServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(ILogRecordService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public ILogRecordService recordService() {
        return new DefaultLogRecordServiceImpl();
    }

    @Bean
    @ConditionalOnMissingBean(IDiffItemsToLogContentService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IDiffItemsToLogContentService diffItemsToLogContentService() {
        return new DefaultDiffItemsToLogContentService();
    }

    @Bean
    public ObjectDiffUtil objectDiffUtil() {
        ObjectDiffUtil objectDiffUtil = new ObjectDiffUtil();
        objectDiffUtil.setDiffItemsToLogContentService(diffItemsToLogContentService());
        return objectDiffUtil;
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLogRecord = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (this.enableLogRecord == null) {
            log.info("@EnableCaching is not present on importing class");
        }
    }
}
