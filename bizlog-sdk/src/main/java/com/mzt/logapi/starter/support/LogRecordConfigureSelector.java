package com.mzt.logapi.starter.support;

import com.mzt.logapi.starter.annotation.EnableLogRecord;
import com.mzt.logapi.starter.configuration.LogRecordProxyAutoConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AdviceModeImportSelector;
import org.springframework.lang.Nullable;

/**
 * DATE 6:57 PM
 *
 * @author mzt.
 */
public class LogRecordConfigureSelector extends AdviceModeImportSelector<EnableLogRecord> {
    private static final String ASYNC_EXECUTION_ASPECT_CONFIGURATION_CLASS_NAME =
            "com.mzt.logapi.starter.configuration.LogRecordProxyAutoConfiguration";


    @Override
    @Nullable
    public String[] selectImports(AdviceMode adviceMode) {
        switch (adviceMode) {
            case PROXY:
                return new String[]{LogRecordProxyAutoConfiguration.class.getName()};
            case ASPECTJ:
                return new String[]{ASYNC_EXECUTION_ASPECT_CONFIGURATION_CLASS_NAME};
            default:
                return null;
        }
    }
}