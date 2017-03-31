package com.tree;

/**
 * 用以访问二叉树中的结点
 *
 * @Author zhangweixin
 * @Date 2017/3/27
 */
public interface NodeWrapper<T> {
    int getHeight();

    T getData();
}
