package com.tree;

/**
 * @Author zhangweixin
 * @Date 2017/3/25
 */
public interface TreeVisitor<T> {

    void visitor(NodeWrapper<T> wrapper);
}
