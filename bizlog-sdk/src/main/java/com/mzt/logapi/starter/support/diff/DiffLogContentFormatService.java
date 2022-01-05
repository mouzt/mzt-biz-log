package com.mzt.logapi.starter.support.diff;

import de.danielbechler.diff.node.DiffNode;
import org.springframework.stereotype.Service;

/**
 * @author muzhantong
 * create on 2022/1/4 11:28 上午
 */
@Service
public class DiffLogContentFormatService {

    public String getDiffLogContent(String filedLogName, DiffNode.State state, Object o1, Object o2, String functionName) {
        return "";
    }
}
