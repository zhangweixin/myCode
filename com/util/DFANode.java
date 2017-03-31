package com.util;

import com.tree.AVLTree;
import com.tree.BinarySortTree;
import com.tree.Node;

/**
 * @Author zhangweixin
 * @Date 2017/3/24
 */
public class DFANode implements Node<Character> {

    private Character data;
    private BinarySortTree<Character> nextStates;
    private int height;
    private boolean intactWord;
    private DFANode rightChild;
    private DFANode leftChild;

    public DFANode(Character data) {
        this.data = data;
        this.height = 1;
    }


    @Override
    public void setData(Character data) {
        this.data = data;
    }

    @Override
    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void setRightChild(Node<Character> rightChild) {
        if (rightChild != null && !(rightChild instanceof DFANode)) {
            throw new RuntimeException("rightChild must be DFANode.class instance");
        }
        this.rightChild = (DFANode) rightChild;
    }

    @Override
    public void setLeftChild(Node<Character> leftChild) {
        if (leftChild != null && !(leftChild instanceof DFANode)) {
            throw new RuntimeException("leftChild must be DFANode.class instance");
        }
        this.leftChild = (DFANode) leftChild;
    }

    @Override
    public Node<Character> getLeftChild() {
        return leftChild;
    }

    @Override
    public Node<Character> getRightChild() {
        return rightChild;
    }

    @Override
    public Character getData() {
        return data;
    }

    @Override
    public int getHeight() {
        return height;
    }

    /**
     * 获取下一步要匹配的状态
     *
     * @return
     */
    public BinarySortTree<Character> getNextStates() {
        return nextStates == null ? new AVLTree<>(new DFANodeFactory()) : nextStates;
    }

    public void setNextStates(BinarySortTree<Character> nextStates) {
        this.nextStates = nextStates;
    }


    @Override
    public int compareTo(Node<Character> o) {
        return data.compareTo(o.getData());
    }

    /**
     * 是否是一个完整敏感词链的结尾
     *
     * @return
     */
    public boolean isIntactWord() {
        return intactWord;
    }

    /**
     * 设置结点为一个完整敏感词链的结尾
     *
     * @param intactWord
     */
    public void setIntactWord(boolean intactWord) {
        this.intactWord = intactWord;
    }
}
