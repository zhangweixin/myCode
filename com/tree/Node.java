package com.tree;

/**
 * @Author zhangweixin
 * @Date 2017/3/24
 */
public interface Node<T> extends Comparable<Node<T>> {
    Node<T> getLeftChild();

    Node<T> getRightChild();

    void setRightChild(Node<T> rightChild);

    void setLeftChild(Node<T> leftChild);

    void setData(T data);

    T getData();

    void setHeight(int height);

    /**
     * 结点高度:空节点高度为0，单个结点高度为1，新结点高度也为1
     *
     * @return
     */
    int getHeight();

}
