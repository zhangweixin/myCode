package com.util;

import com.tree.Node;
import com.tree.NodeFactory;

/**
 * @Author zhangweixin
 * @Date 2017/3/29
 */
public class DFANodeFactory implements NodeFactory<Character> {
    @Override
    public Node<Character> newNode(Character data) {
        return new DFANode(data);
    }
}
