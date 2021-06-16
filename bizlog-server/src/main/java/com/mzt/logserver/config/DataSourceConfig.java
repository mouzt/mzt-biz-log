package com.mzt.logserver.config;

import com.mzt.logserver.context.TenantEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @ConfigurationProperties(prefix = "datasource.test1")
    @Bean("test1Source")
    public DataSource test1Source() {
        return DataSourceBuilder.create().build();
    }

    @ConfigurationProperties(prefix = "datasource.test2")
    @Bean("test2Source")
    public DataSource test2Source() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(@Qualifier("test1Source") DataSource test1Source, @Qualifier("test2Source") DataSource test2Source) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        //配置多数据源
        Map<Object, Object> dbMap = new HashMap<>();
        dbMap.put(TenantEnum.TEST1.name(), test1Source);
        dbMap.put(TenantEnum.TEST2.name(), test2Source);
        dynamicDataSource.setTargetDataSources(dbMap);
        dynamicDataSource.setDefaultTargetDataSource(test1Source());
        return dynamicDataSource;
    }
}
