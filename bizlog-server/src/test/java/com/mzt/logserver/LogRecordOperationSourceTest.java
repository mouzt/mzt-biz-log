package com.mzt.logserver;

import com.mzt.logapi.starter.support.aop.LogRecordOperationSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class LogRecordOperationSourceTest {

    private LogRecordOperationSource source = new LogRecordOperationSource();

    @Test
    public void testGetInterfaceMethodIfPossible() throws NoSuchMethodException {
        // given
        Method method = MyService.class.getMethod("doStuff");

        // when
        Method ifMethod = LogRecordOperationSource.getInterfaceMethodIfPossible(method);

        // then
        Assertions.assertNotNull(ifMethod);
        Assertions.assertEquals(MyIf.class.getMethod("doStuff"), ifMethod);
    }

    @Test
    public void testGetInterfaceMethodIfPossible_NotPublic() throws NoSuchMethodException {
        // given
        Method method = MyService.class.getMethod("doStuff");

        // when
        Method ifMethod = LogRecordOperationSource.getInterfaceMethodIfPossible(method);

        // then
        Assertions.assertNotNull(ifMethod);
        Assertions.assertEquals(MyIf.class.getMethod("doStuff"), ifMethod);
    }

    @Test
    public void testGetInterfaceMethodIfPossible_Interface() throws NoSuchMethodException {
        // given
        Method method = MyIf.class.getMethod("doStuff");

        // when
        Method ifMethod = LogRecordOperationSource.getInterfaceMethodIfPossible(method);

        // then
        Assertions.assertNotNull(ifMethod);
        Assertions.assertEquals(MyIf.class.getMethod("doStuff"), ifMethod);
    }

    @Test
    public void testGetInterfaceMethodIfPossible_Object() throws NoSuchMethodException {
        // given
        Method method = Object.class.getMethod("toString");

        // when
        Method ifMethod = LogRecordOperationSource.getInterfaceMethodIfPossible(method);

        // then
        Assertions.assertNotNull(ifMethod);
        Assertions.assertEquals(Object.class.getMethod("toString"), ifMethod);
    }

    interface MyIf {
        void doStuff();
    }

    static class MyService implements MyIf {

        @Override
        public void doStuff() {
            // nothing
        }

        void anotherStuff() {
            // nothing
        }
    }
}