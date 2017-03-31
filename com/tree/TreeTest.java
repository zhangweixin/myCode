package com.tree;

import org.junit.Test;

/**
 * @Author zhangweixin
 * @Date 2017/3/27
 */
public class TreeTest {

    @Test
    public void testAvlInert() {
        AVLTree<Integer> tree = new AVLTree<>(data -> {
            Node<Integer> node = new IntegerNode();
            node.setData(data);
            return node;
        });

        Integer[] ints = new Integer[]{6, 2, 2, 1, 3, 3, 7, 4, 4, 8};
        for (Integer integer : ints) {
            tree.insert(integer);
        }

        tree.delete(6);
        tree.visitTree(wrapper -> {
            System.out.println(wrapper);
        });
        System.out.println("<---------------||------------------>");
        tree.delete(2);
        tree.visitTree(wrapper -> {
            System.out.println(wrapper);
        });
        System.out.println(tree.maxValue());
        System.out.println(tree.minValue());
        System.out.println(tree.count());
        System.out.println(tree.isEmpty());
    }

    static class IntegerNode implements Node<Integer> {

        private Integer data;

        private int height;

        private Node<Integer> leftChild;

        private Node<Integer> rightChild;

        @Override

        public Node<Integer> getLeftChild() {
            return leftChild;
        }

        @Override
        public Node<Integer> getRightChild() {
            return rightChild;
        }

        @Override
        public void setRightChild(Node<Integer> rightChild) {
            this.rightChild = rightChild;
        }

        @Override
        public void setLeftChild(Node<Integer> leftChild) {
            this.leftChild = leftChild;
        }

        @Override
        public void setData(Integer data) {
            this.data = data;
        }

        @Override
        public Integer getData() {
            return data;
        }

        @Override
        public void setHeight(int height) {
            this.height = height;
        }

        @Override
        public int getHeight() {
            return height;
        }

        @Override
        public int compareTo(Node<Integer> o) {
            return data.compareTo(o.getData());
        }

        @Override
        public String toString() {
            return "IntegerNode{" +
                    "data=" + data +
                    ", height=" + height +
                    '}';
        }
    }
}
