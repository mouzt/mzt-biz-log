package com.mzt.logapi.starter.support;

import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;
import org.springframework.lang.Nullable;

import com.mzt.logapi.starter.annotation.EnableLogRecord;
import com.mzt.logapi.starter.configuration.LogRecordProxyAutoConfiguration;

/**
 * DATE 6:57 PM
 *
 * @author mzt.
 */
public class LogRecordConfigureSelector extends AdviceModeImportSelector<EnableLogRecord> {

    @Override
    @Nullable
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{AutoProxyRegistrar.class.getName(), LogRecordProxyAutoConfiguration.class.getName()};
            case ASPECTJ:
                return new String[] {LogRecordProxyAutoConfiguration.class.getName()};
            default:
                return null;
        }
    }
}