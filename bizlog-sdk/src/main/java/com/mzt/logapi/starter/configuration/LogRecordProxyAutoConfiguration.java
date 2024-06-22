package com.mzt.logapi.starter.configuration;

import com.mzt.logapi.service.*;
import com.mzt.logapi.service.impl.*;
import com.mzt.logapi.starter.annotation.EnableLogRecord;
import com.mzt.logapi.starter.diff.DefaultDiffItemsToLogContentService;
import com.mzt.logapi.starter.diff.IDiffItemsToLogContentService;
import com.mzt.logapi.starter.support.aop.BeanFactoryLogRecordAdvisor;
import com.mzt.logapi.starter.support.aop.LogRecordInterceptor;
import com.mzt.logapi.starter.support.aop.LogRecordOperationSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * @author muzhantong
 * create on 2020/6/12 10:41 上午
 */
@Configuration
@EnableConfigurationProperties({LogRecordProperties.class})
@Slf4j
public class LogRecordProxyAutoConfiguration implements ImportAware {

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

    @DependsOn("logRecordInterceptor")
    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public BeanFactoryLogRecordAdvisor logRecordAdvisor(LogRecordInterceptor logRecordInterceptor) {
        BeanFactoryLogRecordAdvisor advisor =
                new BeanFactoryLogRecordAdvisor();
        advisor.setLogRecordOperationSource(logRecordOperationSource());
        advisor.setAdvice(logRecordInterceptor);
        advisor.setOrder(enableLogRecord.getNumber("order"));
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean(ILogRecordPerformanceMonitor.class)
    public ILogRecordPerformanceMonitor logRecordPerformanceMonitor() {
        return new DefaultLogRecordPerformanceMonitor();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public LogRecordInterceptor logRecordInterceptor(LogRecordProperties logRecordProperties) {
        LogRecordInterceptor interceptor = new LogRecordInterceptor();
        interceptor.setLogRecordOperationSource(logRecordOperationSource());
        interceptor.setTenant(enableLogRecord.getString("tenant"));
        interceptor.setJoinTransaction(enableLogRecord.getBoolean("joinTransaction"));
        interceptor.setDiffSameWhetherSaveLog(logRecordProperties.getDiffLog());
        //interceptor.setLogFunctionParser(logFunctionParser(functionService));
        //interceptor.setDiffParseFunction(diffParseFunction);
        interceptor.setLogRecordPerformanceMonitor(logRecordPerformanceMonitor());
        return interceptor;
    }

//    @Bean
//    public LogFunctionParser logFunctionParser(IFunctionService functionService) {
//        return new LogFunctionParser(functionService);
//    }

    @Bean
    public DiffParseFunction diffParseFunction(IDiffItemsToLogContentService diffItemsToLogContentService,
                                               LogRecordProperties logRecordProperties) {
        DiffParseFunction diffParseFunction = new DiffParseFunction();
        diffParseFunction.setDiffItemsToLogContentService(diffItemsToLogContentService);
        // issue#111
        diffParseFunction.addUseEqualsClass(LocalDateTime.class);
        if (!StringUtils.isEmpty(logRecordProperties.getUseEqualsMethod())) {
            diffParseFunction.addUseEqualsClass(Arrays.asList(logRecordProperties.getUseEqualsMethod().split(",")));
        }
        return diffParseFunction;
    }

    @Bean
    @ConditionalOnMissingBean(IDiffItemsToLogContentService.class)
    @Role(BeanDefinition.ROLE_APPLICATION)
    public IDiffItemsToLogContentService diffItemsToLogContentService(LogRecordProperties logRecordProperties) {
        return new DefaultDiffItemsToLogContentService(logRecordProperties);
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

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.enableLogRecord = AnnotationAttributes.fromMap(
                importMetadata.getAnnotationAttributes(EnableLogRecord.class.getName(), false));
        if (this.enableLogRecord == null) {
            log.info("EnableLogRecord is not present on importing class");
        }
    }
}
