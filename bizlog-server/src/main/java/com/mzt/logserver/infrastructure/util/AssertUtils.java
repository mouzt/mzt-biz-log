package com.mzt.logserver.infrastructure.util;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;

/**
 * @author feichaoyu
 * created on 2021/11/19 11:08 上午
 */
public class AssertUtils {

    /**
     * 比对两个对象的属性是否一致
     * <p>
     * 注意，如果 expected 存在的属性，actual 不存在的时候，会进行忽略
     *
     * @param expected     期望对象
     * @param actual       实际对象
     * @param ignoreFields 忽略的属性数组
     */
    public static void assertPojoEquals(Object expected, Object actual, String... ignoreFields) {
        Field[] expectedFields = ReflectUtil.getFields(expected.getClass());
        Arrays.stream(expectedFields).forEach(expectedField -> {
            // 忽略 jacoco 自动生成的 $jacocoData 属性的情况. 参见 https://km.sankuai.com/page/328514862
            if (expectedField.isSynthetic()) {
                return;
            }
            // 如果是忽略的属性，则不进行比对
            if (ArrayUtil.contains(ignoreFields, expectedField.getName())) {
                return;
            }
            // 忽略不存在的属性
            Field actualField = ReflectUtil.getField(actual.getClass(), expectedField.getName());
            if (actualField == null) {
                return;
            }
            // 比对
            Object expectedValue = ReflectUtil.getFieldValue(expected, expectedField);
            Object actualValue = ReflectUtil.getFieldValue(actual, actualField);
            if (expectedValue instanceof Collection) {
                // TODO 稍后完善为空的情况
                Collection<?> expectedValues = (Collection<?>) expectedValue;
                Collection<?> actualValues = (Collection<?>) actualValue;
                // 比较集合的大小
                assertEquals(String.format("Field(%s) 大小不匹配", expectedField.getName()), expectedValues.size(), actualValues.size());
                int index = 0;
                // 遍历每个元素
                for (Iterator<?> expectedIterator = expectedValues.iterator(), actualIterator = expectedValues.iterator(); expectedIterator.hasNext(); ) {
                    Object expectedIteratorItem = expectedIterator.next();
                    Object actualIteratorItem = actualIterator.next();
                    // 处理是简单类型的时候
                    if (ClassUtil.isSimpleValueType(expected.getClass())) {
                        assertEquals(String.format("Field(%s) 的 Index(%d) 不匹配", expectedField.getName(), index), expectedIteratorItem, actualIteratorItem);
                        // 处理是 Pojo 类型的时候
                    } else {
                        assertPojoEquals(expectedIteratorItem, actualIteratorItem, ignoreFields);
                    }
                }
            } else {
                assertEquals(String.format("Field(%s) 不匹配", expectedField.getName()), expectedValue, actualValue);
            }
        });
    }

}
