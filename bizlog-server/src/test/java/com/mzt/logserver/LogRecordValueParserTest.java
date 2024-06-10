package com.mzt.logserver;

import com.mzt.logapi.starter.support.parse.LogFunctionParser;
import com.mzt.logapi.starter.support.parse.LogRecordValueParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.expression.EvaluationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LogRecordValueParserTest {

    private LogRecordValueParser logRecordValueParser;
    private LogFunctionParser logFunctionParser;
    private EvaluationContext evaluationContext;

    @BeforeEach
    public void setUp() {
        logRecordValueParser = new LogRecordValueParser();
        logFunctionParser = mock(LogFunctionParser.class);
        evaluationContext = mock(EvaluationContext.class);
        logRecordValueParser.setLogFunctionParser(logFunctionParser);
    }

    @Test
    public void processBeforeExecuteFunctionTemplateReturnsExpectedMap() {
        when(logFunctionParser.beforeFunction(anyString())).thenReturn(true);
        when(logFunctionParser.getFunctionReturnValue(any(), any(), any(), any())).thenReturn("returnValue");
        when(logFunctionParser.getFunctionCallInstanceKey(any(), any())).thenReturn("functionKey");

        Map<String, String> result = logRecordValueParser.processBeforeExecuteFunctionTemplate(
                Arrays.asList("template"), this.getClass(), this.getClass().getMethods()[0], new Object[] {});

        assertEquals(1, result.size());
        assertTrue(result.containsKey("functionKey"));
        assertEquals("returnValue", result.get("functionKey"));
    }

    @Test
    public void processBeforeExecuteFunctionTemplateSkipsRetAndErrorMsgExpressions() {
        Map<String, String> result = logRecordValueParser.processBeforeExecuteFunctionTemplate(
                Arrays.asList("{functionName{#_ret}}", "{functionName{#_errorMsg}}"), this.getClass(), this.getClass().getMethods()[0], new Object[] {});

        assertTrue(result.isEmpty());
    }

    @Test
    public void processBeforeExecuteFunctionTemplateReturnsEmptyMapForNonMatchingTemplates() {
        Map<String, String> result = logRecordValueParser.processBeforeExecuteFunctionTemplate(
                Arrays.asList("nonMatchingTemplate"), this.getClass(), this.getClass().getMethods()[0], new Object[] {});

        assertTrue(result.isEmpty());
    }
}