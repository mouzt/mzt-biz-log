package com.mzt.logapi.server.support;

import com.mzt.logapi.server.configuration.LogRecordProxyAutoConfiguration;
import com.mzt.logapi.server.annotation.EnableLogRecord;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.context.annotation.AutoProxyRegistrar;

/**
 * DATE 6:57 PM
 *
 * @author mzt.
 */
public class LogRecordConfigureSelector extends AdviceModeImportSelector<EnableLogRecord> {
    @Override
    protected String[] selectImports(AdviceMode adviceMode) {
        return new String[]{AutoProxyRegistrar.class.getName(), LogRecordProxyAutoConfiguration.class.getName()};
    }
}