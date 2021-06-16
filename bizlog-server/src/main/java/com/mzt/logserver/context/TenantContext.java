package com.mzt.logserver.context;

/**
 * @author muzhantong
 * create on 2021/3/14 10:48 下午
 */
public class TenantContext {
    private static InheritableThreadLocal<String> tenantHolder = new InheritableThreadLocal<>();

    public static void setTenant(String tenant) {
        tenantHolder.set(tenant);
    }

    public static String getTenant() {
        return tenantHolder.get();
    }

    public static void clear() {
        tenantHolder.remove();
    }
}
