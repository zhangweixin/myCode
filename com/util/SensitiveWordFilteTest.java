package com.util;

import jersey.repackaged.com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

/**
 * @Author zhangweixin
 * @Date 2017/3/31
 */
public class SensitiveWordFilteTest {

    @Test
    public void testInit() {
        List<String> words = Lists.newArrayList("日本","日本人","温家宝","韩国棒子","韩国萨德");
        DFASensitiveWordFilter.setSensitiveWords(words);
        DFASensitiveWordFilter filter = DFASensitiveWordFilter.getInstance();
        System.out.println("您好");
    }

}
