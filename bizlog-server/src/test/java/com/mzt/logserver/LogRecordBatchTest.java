package com.mzt.logserver;

import com.mzt.logapi.starter.annotation.LogRecord;
import com.mzt.logserver.infrastructure.constants.LogRecordType;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class LogRecordBatchTest {

    public static class TreeNode{
        int value;
        TreeNode left;
        TreeNode right;
    }

    public boolean isChild(TreeNode parent, TreeNode child) {
        if (parent == null && child == null) {
            return true;
        }
        if (parent == null || child == null) {
            return false;
        }
        if (parent.value == child.value) {
            return isChild(parent.left, child.left) && isChild(parent.right, child.right);
        }
        return isChild(parent.left, child) || isChild(parent.right, child);
    }
}
