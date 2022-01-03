package com.mzt.logapi.starter.support.diff;

import de.danielbechler.diff.node.DiffNode;

/**
 * @author muzhantong
 * create on 2022/1/3 8:26 下午
 */
public interface IDiffItemsToLogContentService {

    String toLogContent(DiffNode diffNode, Object o1, Object o2);
}
