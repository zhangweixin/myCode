package com.test;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * k路归并+败者树实现外排序
 * @Author zhangweixin
 * @Date 2017/2/9
 */
public class MultiMergeTest {

    @Test
    public void mergeSort() throws IOException {
        File file = testGenerateData("bigData.txt", 80);
        Resources.readLines(file.toURI().toURL(), Charset.defaultCharset(), new CustomLineProcessor(file, 10));
    }


    static class CustomLineProcessor implements LineProcessor<Void> {

        private int layer;
        private int layerNum;
        private int threshold;
        private File file;
        private List<Long> tempCache = new LinkedList<>();

        public CustomLineProcessor(File file, int threshold) {
            layer = 0;
            layerNum = 0;
            this.threshold = threshold;
            this.file = file;
        }

        @Override
        public boolean processLine(String line) throws IOException {
            tempCache.add(Long.parseLong(line));
            if (tempCache.size() == threshold) {
                sortAndStore();
            }
            return true;
        }

        private void sortAndStore() throws IOException {
            Collections.sort(tempCache);
            File parentFile = file.getParentFile();
            File destFile = new File(parentFile, layer + "_" + (layerNum++) + "_" + file.getName());
            BufferedWriter writer = null;
            try {
                destFile.createNewFile();
                writer = Files.newWriter(destFile, Charset.defaultCharset());
                for (Long num : tempCache) {
                    writer.write(num.toString());
                    writer.newLine();
                }
            } finally {
                writer.close();
                tempCache.clear();
            }
        }

        private void merge() throws IOException {
            File parent = file.getParentFile();
            File[] files = parent.listFiles((dir, name) -> {
                if (name.startsWith(String.valueOf(layer - 1))) {
                    return true;
                }
                return false;
            });
            MultiMerger.merge(Arrays.asList(files), new File(parent, "final.txt"));
        }

        @Override
        public Void getResult() {
            try {
                if (!tempCache.isEmpty()) {
                    sortAndStore();
                }
                ++layer;
                layerNum = 0;
                merge();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public File testGenerateData(String fileName, int num) throws IOException {
        File tempDir = Files.createTempDir();
        File file = new File(tempDir, fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        BufferedWriter writer = Files.newWriter(file, Charset.defaultCharset());
        Random random = new Random();
        if (num <= 0) {
            num = 10;
        }

        for (int i = 0; i < num; i++) {
            long temp = random.nextLong();
            if (temp < 0) {
                temp = ~temp + 1;
            }
            writer.write(String.valueOf(temp));
            writer.newLine();
        }
        writer.close();
        return file;
    }

    /**
     * 多路归并
     */
    static class MultiMerger {
        public static void merge(List<File> files, File target) throws IOException {
            List<Participant> participants = new ArrayList<>();
            try {
                for (File file : files) {
                    Participant participant = new Participant(file);
                    participants.add(participant);
                }
                LoserTree tree = new LoserTree(participants);
                tree.multiMerge(target);
            } finally {
                for (Participant participant : participants) {
                    participant.finish();
                }

            }
        }
    }

    /**
     * 败者树结构
     */
    static class LoserTree {
        private Comparer[] comparers;
        private List<Participant> participants;
        private int[] losers = null;
        private int compareTime;

        public LoserTree(List<Participant> participants) {
            this.participants = participants;
        }

        public Comparer getMinComparer() {
            return comparers[losers[0]];
        }

        public void multiMerge(File target) throws IOException {
            BufferedWriter writer = null;
            try {
                writer = Files.newWriter(target, Charset.defaultCharset());
                initTree();
                while (compareTime > 0) {
                    Comparer comparer = getMinComparer();
                    write(writer, comparer);
                    fillData();
                }
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }

        private void fillData() throws IOException {
            Comparer old = getMinComparer();
            comparers[losers[0]] = participants.get(losers[0]).getComparer();
            adjustTree(losers[0]);
        }

        private void write(BufferedWriter writer, Comparer comparer) throws IOException {
            writer.write(comparer.toString());
            writer.newLine();
        }

        private void initTree() throws IOException {
            int k = participants.size();
            comparers = new Comparer[k + 1];
            losers = new int[k];
            for (int i = 0; i < k; ++i) {
                comparers[i] = participants.get(i).getComparer();
                losers[i] = k;
            }
            comparers[k] = new LongComparer("", false);
            comparers[k].setSentry(true);
            compareTime = k;

            for (int i = k - 1; i >= 0; i--) {
                adjustTree(i);
            }
        }

        private void adjustTree(int loserIndex) {
            int parentIndex = (loserIndex + comparers.length - 1) / 2;
            if (comparers[loserIndex].isDone()) {
                compareTime--;
            }

            while (parentIndex > 0) {
                if (!comparers[losers[parentIndex]].isDone()) {
                    if (comparers[loserIndex].isDone() || (comparers[loserIndex].compare(comparers[losers[parentIndex]]) > 0)) {
                        int temp = loserIndex;
                        loserIndex = losers[parentIndex];
                        losers[parentIndex] = temp;
                    }

                }
                parentIndex = parentIndex / 2;
            }
            losers[0] = loserIndex;
        }
    }

    /**
     * 多路归并参与者
     */
    static class Participant {
        private BufferedReader reader;

        public Participant(File file) throws FileNotFoundException {
            reader = Files.newReader(file, Charset.defaultCharset());
        }

        public Comparer getComparer() throws IOException {
            String line = reader.readLine();
            Comparer comparer = null;
            if (Strings.isNullOrEmpty(line)) {
                comparer = new LongComparer(line, true);
            } else {
                comparer = new LongComparer(line, false);
            }

            return comparer;
        }

        public void finish() {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 败者树多路比较者接口
     *
     * @param <T>
     */
    interface Comparer<T> {
        int compare(Comparer<T> target);

        boolean isDone();

        T getValue();

        boolean isSentry();

        void setSentry(boolean sentry);
    }

    static class LongComparer implements Comparer<Long> {

        private Long value;
        private boolean isDone;
        private boolean isSentry;

        public LongComparer(String line, boolean isDone) {
            value = Strings.isNullOrEmpty(line) ? -1 : Long.valueOf(line);
            this.isDone = isDone;
            isSentry = false;
        }

        @Override
        public int compare(Comparer<Long> target) {
            return target.isSentry() ? 1 : value.compareTo(target.getValue());
        }

        @Override
        public boolean isDone() {
            return isDone;
        }

        @Override
        public Long getValue() {
            return value;
        }

        @Override
        public boolean isSentry() {
            return isSentry;
        }

        @Override
        public void setSentry(boolean sentry) {
            isSentry = sentry;
        }


        @Override
        public String toString() {
            return value.toString();
        }
    }
}
