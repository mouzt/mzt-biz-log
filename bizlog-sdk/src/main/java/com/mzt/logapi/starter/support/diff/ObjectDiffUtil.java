package com.mzt.logapi.starter.support.diff;

import de.danielbechler.diff.ObjectDifferBuilder;
import de.danielbechler.diff.node.DiffNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author muzhantong
 * create on 2022/1/3 8:20 下午
 */
public class ObjectDiffUtil {

    private static IDiffItemsToLogContentService diffItemsToLogContentService;

    @Autowired
    public void setDiffItemsToLogContentService(IDiffItemsToLogContentService IDiffItemsToLogContentService) {
        ObjectDiffUtil.diffItemsToLogContentService = IDiffItemsToLogContentService;
    }

    public static String diff(Object o1, Object o2) {
        DiffNode diffNode = ObjectDifferBuilder.buildDefault().compare(o1, o2);
        return diffItemsToLogContentService.toLogContent(diffNode, o1, o2);
    }
}
