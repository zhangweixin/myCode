package com.tree;

/**
 * @Author zhangweixin
 * @Date 2017/3/27
 */
public class DefaultNodeWrapper<T> implements NodeWrapper<T> {

    private Node<T> node;

    public DefaultNodeWrapper(Node<T> node) {
        this.node = node;
    }

    @Override
    public int getHeight() {
        return node.getHeight();
    }

    @Override
    public T getData() {
        return node.getData();
    }

    @Override
    public String toString() {
        return node.toString();
    }
}
