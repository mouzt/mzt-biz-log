package com.mzt.logapi.starter.diff;

import de.danielbechler.diff.node.DiffNode;

/**
 * @author muzhantong
 * create on 2022/1/3 8:26 下午
 */
public interface IDiffItemsToLogContentService {

    String toLogContent(DiffNode diffNode, final Object o1, final Object o2);
}
