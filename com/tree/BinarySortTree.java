package com.tree;

/**
 * @Author zhangweixin
 * @Date 2017/3/25
 */
public interface BinarySortTree<T> {

    /**
     * 插入数据
     *
     * @param data
     */
    void insert(T data);


    /**
     * 查找数据
     *
     * @param data
     * @return 返回包含数据的结点，如果不存在返回null
     */
    Node<T> search(T data);

    /**
     * 删除数据并返回结点
     *
     * @param data
     * @return
     */
    void delete(T data);


    /**
     * 更新树中某个结点的值
     *
     * @param newData 要更新的值
     * @param oldData 存在的旧值
     */
    void update(T newData, T oldData);

    /**
     * 返回树是否是空树
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 返回树中结点个数
     *
     * @return
     */
    int count();

    /**
     * 返回二叉排序树中最大值
     *
     * @return
     */
    T maxValue();

    /**
     * 返回二叉排序树中最小值
     *
     * @return
     */
    T minValue();

    /**
     * 遍历树中的结点
     *
     * @param visitor
     */
    void visitTree(TreeVisitor<T> visitor);
}
