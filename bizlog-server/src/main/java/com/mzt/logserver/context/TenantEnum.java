package com.mzt.logserver.context;

import java.util.Objects;

public enum TenantEnum {
    TEST1,
    TEST2,
    ;

    public static TenantEnum keyOf(String tenantId) {
        for (TenantEnum value : TenantEnum.values()) {
            if (Objects.equals(tenantId, value.name())) {
                return value;
            }
        }
        return null;
    }
}
