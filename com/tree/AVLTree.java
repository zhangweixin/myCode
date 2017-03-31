package com.tree;

import com.google.common.collect.Lists;

import java.util.Queue;

/**
 * 平衡二叉搜索树(假设叶子结点高度为1)
 *
 * @Author zhangweixin
 * @Date 2017/3/24
 */
public class AVLTree<T> implements BinarySortTree<T> {

    private Node<T> root;

    private NodeFactory<T> nodeFactory;

    private int count;

    public AVLTree(NodeFactory<T> nodeFactory) {
        this.nodeFactory = nodeFactory;
        count = 0;
    }

    @Override
    public void insert(T data) {
        if (data == null) {
            return;
        }

        Node<T> newNode = nodeFactory.newNode(data);
        newNode.setHeight(1);
        if (root == null) {
            root = newNode;
            count++;
            return;
        }
        root = doInsert(root, newNode);
    }


    @Override
    public Node<T> search(T data) {
            return doSearch(root, nodeFactory.newNode(data));
    }

    @Override
    public void delete(T data) {
        if (search(data) != null) {
            root = doDelete(root, nodeFactory.newNode(data));
        }
    }

    @Override
    public void update(T newData, T oldData) {
        delete(oldData);
        insert(newData);
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public T maxValue() {
        T max = null;
        if (!isEmpty()) {
            max = getMaxMin(1, root).getData();
        }
        return max;
    }

    @Override
    public T minValue() {
        T min = null;
        if (!isEmpty()) {
            min = getMaxMin(0, root).getData();
        }
        return min;
    }

    private Node<T> getMaxMin(int state, Node<T> node) {
        if (node != null) {
            Node<T> temp = node;
            do {
                node = temp;
                if (state == 0) {
                    temp = temp.getLeftChild();
                } else {
                    temp = temp.getRightChild();
                }
            } while (temp != null);
        }
        return node;
    }

    @Override
    public void visitTree(TreeVisitor<T> visitor) {
        Queue<Node<T>> queue = Lists.newLinkedList();
        if (root != null) {
            queue.offer(root);
        }

        while (!queue.isEmpty()) {
            Node<T> node = queue.poll();
            if (node.getLeftChild() != null) {
                queue.offer(node.getLeftChild());
            }

            if (node.getRightChild() != null) {
                queue.offer(node.getRightChild());
            }
            visitor.visitor(new DefaultNodeWrapper<>(node));
        }
    }

    /**
     * 内部真正执行删除结点的方法
     *
     * @param currentNode
     * @param target
     * @return
     */
    private Node<T> doDelete(Node<T> currentNode, Node<T> target) {
        /**
         * 删除原理:找到要删除结点<br>
         * 1.如果要删除的结点右子结点存在，找到以右子结点为根的子树中最左子节点，用此节点替换要删除的结点，
         *   然后在右子树中删除最左子结点(以此递归直到出现2情况停止递归删除结束)
         * 2.如果要删除的结点右子结点不存在，直接返回左子结点(终止递归，由于右子树本就是一个AVL树不需要计算子树高度重新平衡树直接返回即可)
         * 在结束本层递归返回上层递归之前除了上面2情况外都需要重新计算结点高度如果有需要，需重新平衡树
         */
        int comparedValue = currentNode.compareTo(target);
        int state = 0;
        if (comparedValue > 0) {
            currentNode.setLeftChild(doDelete(currentNode.getLeftChild(), target));
        } else if (comparedValue < 0) {
            currentNode.setRightChild(doDelete(currentNode.getRightChild(), target));
            state = 1;
        } else {
            //找到要删除的结点
            if (currentNode.getRightChild() != null) {
                Node<T> newRoot = currentNode.getRightChild();
                while (newRoot.getLeftChild() != null) {
                    newRoot = newRoot.getLeftChild();
                }
                currentNode.setData(newRoot.getData());
                currentNode.setRightChild(doDelete(currentNode.getRightChild(), currentNode));
                state = 1;
            } else {
                count--;
                return currentNode.getLeftChild();
            }

        }

        return adjustBalance(state, currentNode);
    }

    /**
     * 删除结点后调整树的平衡
     *
     * @param state       0:左子树上删除 1:右子树上删除
     * @param currentNode
     * @return
     */
    private Node<T> adjustBalance(int state, Node<T> currentNode) {
        if (Math.abs(getHeightDifference(currentNode.getLeftChild(), currentNode.getRightChild())) >= 2) {
            //左子树上删除相当于在右子树上插入(左子树高度降低相当于在右子树上插入一个结点导致高度发生变化)
            if (state == 0) {
                if (currentNode.getRightChild().getRightChild() != null) {
                    currentNode = leftRotate(currentNode);
                } else {
                    currentNode = rightLeftRotate(currentNode);
                }
            } else if (state == 1) {
                //在右子树上删除相当于在左子树上插入
                if (currentNode.getLeftChild().getLeftChild() != null) {
                    currentNode = rightRotate(currentNode);
                } else {
                    currentNode = leftRightRotate(currentNode);
                }
            }
        } else {
            currentNode.setHeight(getMaxHeight(currentNode.getLeftChild(), currentNode.getRightChild()) + 1);
        }
        return currentNode;
    }


    private Node<T> doSearch(Node<T> currentNode, Node<T> target) {
        if (currentNode == null) {
            return null;
        }

        int comparedValue = currentNode.compareTo(target);
        if (comparedValue < 0) {
            return doSearch(currentNode.getRightChild(), target);
        } else if (comparedValue > 0) {
            return doSearch(currentNode.getLeftChild(), target);
        } else {
            return currentNode;
        }
    }

    /**
     * 内部执行插入操作
     *
     * @param currentNode 执行插入的结点
     * @param newNode     新添加结点
     * @return
     */
    private Node<T> doInsert(Node<T> currentNode, Node<T> newNode) {

        if (currentNode == null) {
            count++;
            return newNode;
        }

        int compareValue = currentNode.compareTo(newNode);
        if (compareValue > 0) {
            currentNode.setLeftChild(doInsert(currentNode.getLeftChild(), newNode));
        } else if (compareValue < 0) {
            currentNode.setRightChild(doInsert(currentNode.getRightChild(), newNode));
        } else {
            //忽略相等情况
            return currentNode;
        }

        Node<T> left = currentNode.getLeftChild();
        Node<T> right = currentNode.getRightChild();
        //超过2调整高度
        if (Math.abs(getHeightDifference(left, right)) >= 2) {
            if (compareValue > 0) {
                //在左子树根节点左边插入时LL型
                if (currentNode.getLeftChild().compareTo(newNode) > 0) {
                    currentNode = rightRotate(currentNode);
                } else {
                    //LR型
                    currentNode = leftRightRotate(currentNode);
                }
            } else if (compareValue < 0) {
                //在右子树根节点的右边插入时(RR型)
                if (currentNode.getRightChild().compareTo(newNode) < 0) {
                    currentNode = leftRotate(currentNode);
                } else {
                    //RL型
                    currentNode = rightLeftRotate(currentNode);
                }
            }
        } else {
            currentNode.setHeight(getMaxHeight(left, right) + 1);
        }
        return currentNode;
    }


    /**
     * 获取子树最大高度
     *
     * @param node1
     * @param node2
     * @return
     */
    private int getMaxHeight(Node<T> node1, Node<T> node2) {
        int height1 = node1 == null ? 0 : node1.getHeight();
        int height2 = node2 == null ? 0 : node2.getHeight();
        return Math.max(height1, height2);
    }

    /**
     * 获取子树高度差
     *
     * @param node1
     * @param node2
     * @return 高度差
     */
    private int getHeightDifference(Node<T> node1, Node<T> node2) {
        int height1 = node1 == null ? 0 : node1.getHeight();
        int height2 = node2 == null ? 0 : node2.getHeight();
        return height1 - height2;
    }

    /**
     * LL型需要右旋平衡树的高度
     *
     * @param node
     * @return
     */
    private Node<T> rightRotate(Node<T> node) {
        /*node结点左孩子成为新的根结点，新的根节点右孩子成为
        旧根结点的左孩子*/
        Node<T> newRoot = node.getLeftChild();
        node.setLeftChild(newRoot.getRightChild());
        newRoot.setRightChild(node);
        //调整高度
        node.setHeight(getMaxHeight(node.getLeftChild(), node.getRightChild()) + 1);
        newRoot.setHeight(getMaxHeight(newRoot.getLeftChild(), newRoot.getRightChild()) + 1);
        return newRoot;
    }

    /**
     * RR型需要左旋平衡树的高度
     *
     * @param node
     * @return
     */
    private Node<T> leftRotate(Node<T> node) {
        /*node结点右孩子成为新的根结点，新的根节左孩子成为
        旧根结点的右孩子*/
        Node<T> newRoot = node.getRightChild();
        node.setRightChild(newRoot.getLeftChild());
        newRoot.setLeftChild(node);
        //调整高度
        node.setHeight(getMaxHeight(node.getLeftChild(), node.getRightChild()) + 1);
        newRoot.setHeight(getMaxHeight(newRoot.getLeftChild(), newRoot.getRightChild()) + 1);
        return newRoot;
    }

    /**
     * LR型需要先左旋左子树变换成LL型再右旋平衡树的高度
     *
     * @param node
     * @return
     */
    private Node<T> leftRightRotate(Node<T> node) {
        //先左旋左子树
        node.setLeftChild(leftRotate(node.getLeftChild()));
        //再右旋整个子树
        return rightRotate(node);
    }

    /**
     * RL型需要先右旋右子树变换成RR型再左旋平衡树的高度
     *
     * @param node
     * @return
     */
    private Node<T> rightLeftRotate(Node<T> node) {
        //先右旋右子树
        node.setRightChild(rightRotate(node.getRightChild()));
        //再左旋整个子树
        return leftRotate(node);
    }
}
