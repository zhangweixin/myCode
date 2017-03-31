package com.util;

import com.google.common.collect.Lists;
import com.tree.AVLTree;
import com.tree.BinarySortTree;
import org.springframework.util.CollectionUtils;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基于变体DFA(状态查找)算法高效敏感词过滤;对于一个敏感词是其中另一个敏感词的前缀这种情况(eg:日本和日本人)<br>
 * 以前缀敏感词匹配为准即当一段文本包含日本人时日本这个敏感词会被处理掉丢下人(因为日本和日本人都是敏感词)
 *
 * @Author zhangweixin
 * @Date 2017/3/29
 */
public class DFASensitiveWordFilter {


    private static final int NO_INIT = 1;
    private static final int INITING = 2;
    private static final int INITED = 3;
    private static volatile int state = NO_INIT;
    private static volatile List<String> sensitiveWords = Lists.newLinkedList();
    private static volatile DFASensitiveWordFilter instance;
    private static ReentrantLock initLock = new ReentrantLock();
    private static ReentrantLock updateLock = new ReentrantLock();

    public static void setSensitiveWords(List<String> words) {
        if (!CollectionUtils.isEmpty(words)) {
            doSetSensitiveWords(words);
        }
    }

    public static void reset() {
        try {
            initLock.lock();
            state = NO_INIT;
        } finally {
            initLock.unlock();
        }
    }

    public static List<String> getSensitiveWords() {
        List<String> copy = Lists.newLinkedList();
        try {
            updateLock.lock();
            copy.addAll(sensitiveWords);
        } finally {
            updateLock.unlock();
        }
        return copy;
    }

    private static void doSetSensitiveWords(List<String> words) {
        try {
            updateLock.lock();
            sensitiveWords.clear();
            sensitiveWords.addAll(words);
        } finally {
            updateLock.unlock();
        }
    }

    public static DFASensitiveWordFilter getInstance() {
        return doCreateInstance();
    }

    private static DFASensitiveWordFilter doCreateInstance() {
        if (state != INITED) {
            try {
                initLock.lock();
                if (state != INITED) {
                    init();
                }
            } finally {
                initLock.unlock();
            }
        }
        return instance;
    }

    private static void init() {
        state = INITING;
        DFASensitiveWordFilter temp = new DFASensitiveWordFilter(getSensitiveWords());
        instance = temp;
        state = INITED;
    }

    /* 敏感词开始状态集合*/
    private BinarySortTree<Character> dfaStates;

    private DFASensitiveWordFilter(List<String> sensitiveWords) {
        if (!CollectionUtils.isEmpty(sensitiveWords)) {
            if (dfaStates == null) {
                dfaStates = new AVLTree<>(new DFANodeFactory());
            }

            for (String word : sensitiveWords) {
                DFANode state = null;
                BinarySortTree<Character> states = dfaStates;
                for (Character c : word.toCharArray()) {
                    states.insert(c);
                    state = (DFANode) states.search(c);
                    states = state.getNextStates();
                }
                state.setIntactWord(true);
            }
        }
    }


    /**
     * 检查给定的字符串是否存在敏感词
     *
     * @param text
     * @return
     */
    public boolean existSensitiveWord(String text) {
        return existSensitiveWord(text.toCharArray());
    }

    /**
     * 检查给定的字符数组中是否存在敏感词
     *
     * @param chars
     * @return
     */
    public boolean existSensitiveWord(char[] chars) {
        BinarySortTree<Character> states = dfaStates;
        CharSource charSource = new CharSource(chars);
        //标识匹配状态位置
        int position = 0;
        do {
            Character character = charSource.next();
            DFANode node = (DFANode) states.search(character);
            if (node != null) {
                states = node.getNextStates();
                if (node.isIntactWord()) {
                    return true;
                }
                position++;
            } else {
                if (position > 0) {
                    charSource.back();
                }
                states = dfaStates;
                position = 0;
            }
        } while (!charSource.isEnd());
        return false;
    }

    public String deleteSensitiveWord(String text) throws IOException {
        char[] newChars = deleteSensitiveWord(text.toCharArray());
        return new String(newChars);
    }

    public char[] deleteSensitiveWord(char[] chars) throws IOException {

        CharSource charSource = new CharSource(chars);
        BinarySortTree<Character> states = dfaStates;
        CharArrayWriter charWriter = new CharArrayWriter(chars.length);
        CharArrayWriter cacheWriter = new CharArrayWriter(chars.length);
        int position = 0;

        do {
            Character character = charSource.next();
            DFANode node = (DFANode) states.search(character);
            if (node != null) {
                cacheWriter.append(character);
                if (node.isIntactWord()) {
                    cacheWriter.reset();
                    position = 0;
                    states = dfaStates;
                } else {
                    position++;
                    states = node.getNextStates();
                }
            } else {
                if (cacheWriter.size() > 0) {
                    cacheWriter.writeTo(charWriter);
                    cacheWriter.reset();
                }

                if (position > 0) {
                    charSource.back();
                } else {
                    charWriter.write(character);
                }
                //初始化匹配状态开始位置
                position = 0;
                //初始化为开始状态集合
                states = dfaStates;
            }

            if (charSource.isEnd()) {
                if (cacheWriter.size() > 0) {
                    cacheWriter.writeTo(charWriter);
                }
                return charWriter.toCharArray();
            }
        } while (true);
    }

    /**
     * 使用给定的字符替换字符串中的敏感词
     *
     * @param text       进行敏感词替换的字符串
     * @param shieldChar 替换后的字符
     */
    public String shieldSensitiveWord(String text, char shieldChar) throws IOException {
        char[] chars = shieldSensitiveWord(text.toCharArray(), shieldChar);
        return new String(chars);
    }

    /**
     * 使用给定的字符替换字符数组中的敏感词
     *
     * @param chars      进行敏感词替换的字符数组
     * @param shieldChar 替换后的字符
     * @return 返回经过处理的字符数组
     */
    public char[] shieldSensitiveWord(char[] chars, char shieldChar) throws IOException {

        CharSource charSource = new CharSource(chars);
        BinarySortTree<Character> states = dfaStates;
        CharArrayWriter charWriter = new CharArrayWriter(chars.length);
        CharArrayWriter cacheWriter = new CharArrayWriter(chars.length);
        int position = 0;

        do {
            Character character = charSource.next();
            DFANode node = (DFANode) states.search(character);
            if (node != null) {
                cacheWriter.append(character);
                if (node.isIntactWord()) {
                    char[] temp = new char[cacheWriter.size()];
                    Arrays.fill(temp, shieldChar);
                    cacheWriter.reset();
                    charWriter.write(temp);
                    position = 0;
                    states = dfaStates;
                } else {
                    position++;
                    states = node.getNextStates();
                }
            } else {
                if (cacheWriter.size() > 0) {
                    cacheWriter.writeTo(charWriter);
                    cacheWriter.reset();
                }

                if (position > 0) {
                    charSource.back();
                } else {
                    charWriter.write(character);
                }
                //初始化匹配状态开始位置
                position = 0;
                //初始化为开始状态集合
                states = dfaStates;
            }

            if (charSource.isEnd()) {
                if (cacheWriter.size() > 0) {
                    cacheWriter.writeTo(charWriter);
                }
                return charWriter.toCharArray();
            }
        } while (true);
    }

    /**
     * 找出给定字符串中的敏感词
     *
     * @param text 需要查找敏感词的字符串
     * @return 如果没有敏感词返回空List否则返回包含敏感词的List
     */
    public List<String> findSensitiveWords(String text) throws IOException {

        char[] chars = text.toCharArray();
        CharSource charSource = new CharSource(chars);
        BinarySortTree<Character> states = dfaStates;
        CharArrayWriter cacheWriter = new CharArrayWriter(chars.length);
        int position = 0;
        List<String> words = Lists.newLinkedList();

        do {
            Character character = charSource.next();
            DFANode node = (DFANode) states.search(character);
            if (node != null) {
                cacheWriter.append(character);
                if (node.isIntactWord()) {
                    words.add(new String(cacheWriter.toCharArray()));
                    cacheWriter.reset();
                    position = 0;
                    states = dfaStates;
                } else {
                    position++;
                    states = node.getNextStates();
                }
            } else {
                if (position > 0) {
                    cacheWriter.reset();
                    charSource.back();
                    position = 0;
                    states = dfaStates;
                }
            }

            if (charSource.isEnd()) {
                return words;
            }
        } while (true);
    }

    /**
     * 对需要进行敏感词处理的char数组进行封装,提供便利的操作方法
     */
    static class CharSource {
        private char[] chars;
        private int position;

        public CharSource(char[] chars) {
            if (chars == null || chars.length == 0) {
                throw new IllegalArgumentException("chars array must not be empty");
            }
            this.chars = chars;
            this.position = 0;
        }

        public char next() {
            return position < chars.length ? chars[position++] : chars[chars.length - 1];
        }

        public boolean isEnd() {
            return position >= chars.length;
        }

        public void back() {
            if (position > 0) {
                position--;
            }
        }

    }

}
