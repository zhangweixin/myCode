package com.tree;

/**
 * @Author zhangweixin
 * @Date 2017/3/27
 */
@FunctionalInterface
public interface NodeFactory<T> {
    Node<T> newNode(T data);
}
