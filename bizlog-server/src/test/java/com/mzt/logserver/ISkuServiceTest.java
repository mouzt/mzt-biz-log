package com.mzt.logserver;

import cn.hutool.extra.spring.SpringUtil;
import com.mzt.logapi.beans.LogRecord;
import com.mzt.logapi.starter.support.aop.BeanFactoryLogRecordAdvisor;
import com.mzt.logapi.starter.support.aop.LogRecordInterceptor;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import com.mzt.logserver.infrastructure.logrecord.service.JoinTransactionLogRecordService;
import com.mzt.logserver.pojo.ObjectSku;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.Ordered;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.interceptor.BeanFactoryTransactionAttributeSourceAdvisor;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * @author wulang
 **/
public class ISkuServiceTest extends BaseTest {
    @Resource
    private SkuService skuService;
    @Resource
    private JoinTransactionLogRecordService logRecordService;

    @Before
    public void before() {
        LogRecordInterceptor logRecordInterceptor = SpringUtil.getBean(LogRecordInterceptor.class);
        logRecordInterceptor.setLogRecordService(logRecordService);
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder_日志错误_事务回滚() {
        LogRecordInterceptor bean = SpringUtil.getBean(LogRecordInterceptor.class);
        bean.setJoinTransaction(true);
        ObjectSku sku = new ObjectSku();
        sku.setSkuName("ABC");
        sku.setTemplateId(3L);
        sku.setCode("testcode");
        sku.setRemark("日志错误_事务回滚");
        sku.setTaxCategoryNo("qqq");
        sku.setMeasureUnit("件");
        sku.setChildren(Arrays.asList(99L, 88L, 77L));
        try {
            skuService.createObjectSkuNoJoinTransaction(sku);
        } catch (Exception e) {
            // ignore
            // e.printStackTrace();
        } finally {
            List<LogRecord> logRecordList = logRecordService.queryLog(sku.getCode(), LogRecordType.SKU);
            Assert.assertEquals(0, logRecordList.size());
            logRecordService.clean();
        }
    }


    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder_日志错误_事务不回滚() {
        LogRecordInterceptor bean = SpringUtil.getBean(LogRecordInterceptor.class);
        bean.setJoinTransaction(false);
        BeanFactoryTransactionAttributeSourceAdvisor factoryTransactionAttributeSourceAdvisor = SpringUtil.getBean(BeanFactoryTransactionAttributeSourceAdvisor.class);
        factoryTransactionAttributeSourceAdvisor.setOrder(0);
        ObjectSku sku = new ObjectSku();
        sku.setSkuName("ABC");
        sku.setTemplateId(3L);
        sku.setCode("testcode");
        sku.setRemark("日志错误_事务不回滚");
        sku.setTaxCategoryNo("qqq");
        sku.setMeasureUnit("件");
        sku.setChildren(Arrays.asList(99L, 88L, 77L));
        try {
            skuService.createObjectSkuNoJoinTransactionRollBack(sku);
        } catch (Exception e) {
            // ignore
            // e.printStackTrace();
        } finally {
            List<LogRecord> logRecordList = logRecordService.queryLog(sku.getCode(), LogRecordType.SKU);
            Assert.assertEquals(1, logRecordList.size());
            logRecordService.clean();
        }
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder_业务错误_事务回滚() {
        LogRecordInterceptor bean = SpringUtil.getBean(LogRecordInterceptor.class);
        bean.setJoinTransaction(true);
        BeanFactoryTransactionAttributeSourceAdvisor factoryTransactionAttributeSourceAdvisor = SpringUtil.getBean(BeanFactoryTransactionAttributeSourceAdvisor.class);
        factoryTransactionAttributeSourceAdvisor.setOrder(0);
        BeanFactoryLogRecordAdvisor logRecordAdvisor = SpringUtil.getBean(BeanFactoryLogRecordAdvisor.class);
        logRecordAdvisor.setOrder(Ordered.LOWEST_PRECEDENCE);
        ObjectSku sku = new ObjectSku();
        sku.setSkuName("ABC");
        sku.setTemplateId(3L);
        sku.setCode("testcode");
        sku.setRemark("备注");
        sku.setTaxCategoryNo("qqq");
        sku.setMeasureUnit("件");
        sku.setChildren(Arrays.asList(99L, 88L, 77L));
        try {
            skuService.createObjectBusinessError(sku);
        } catch (Exception e) {
            // ignore
            // e.printStackTrace();
        } finally {
            List<LogRecord> logRecordList = logRecordService.queryLog(sku.getCode(), LogRecordType.SKU);
            Assert.assertEquals(0, logRecordList.size());
            logRecordService.clean();
        }
    }

    @Test
    @Sql(scripts = "/sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void createOrder_业务错误_事务不回滚() {
        LogRecordInterceptor bean = SpringUtil.getBean(LogRecordInterceptor.class);
        bean.setJoinTransaction(false);
        ObjectSku sku = new ObjectSku();
        sku.setSkuName("ABC");
        sku.setTemplateId(3L);
        sku.setCode("testcode");
        sku.setRemark("备注");
        sku.setTaxCategoryNo("qqq");
        sku.setMeasureUnit("件");
        sku.setChildren(Arrays.asList(99L, 88L, 77L));
        try {
            skuService.createObjectBusinessError2(sku);
        } catch (Exception e) {
            // ignore
            // e.printStackTrace();
        } finally {
            List<LogRecord> logRecordList = logRecordService.queryLog(sku.getCode(), LogRecordType.SKU);
            Assert.assertEquals(2, logRecordList.size());
            logRecordService.clean();
        }
    }
}
