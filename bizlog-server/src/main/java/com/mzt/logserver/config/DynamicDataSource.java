package com.mzt.logserver.config;
import com.mzt.logserver.context.TenantContext;
import com.mzt.logserver.context.TenantEnum;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author muzhantong
 * create on 2021/3/23 3:41 下午
 */
public class DynamicDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        String appKey = TenantContext.getTenant();
        TenantEnum tenantEnum = TenantEnum.keyOf(appKey);
        if (tenantEnum == null) {
            throw new RuntimeException("租户没有找到，appKey=" + appKey);
        }
        return tenantEnum.name();
    }
}
