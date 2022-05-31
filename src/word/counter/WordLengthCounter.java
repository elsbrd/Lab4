package word.counter;

import example.Document;
import example.Helper;

import java.util.*;
import java.util.concurrent.*;

public class WordLengthCounter {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public Hashtable<Integer, Integer> countWordsLengthInParallel(Document document) {
        Hashtable<Integer, Integer> wordsLengths = new Hashtable<>();

        for (String line : document.getLines()) {
            forkJoinPool.invoke(new LineWordsCounterTask(wordsLengths, line));
        }

        return wordsLengths;
    }

    public RandomVariableCharacteristics countWordsLengthInRealParallel(Document document) {
        var wordsLengths = forkJoinPool.invoke(new BlockWordsCounterTask(document.getLines().toArray(new String[0])));

        var keys = Collections.list(wordsLengths.keys()).stream().mapToInt(i -> i).toArray();
        int wordsSum = wordsLengths.values().stream().mapToInt(i -> i).sum();
        var mathExpected = forkJoinPool.invoke(new BlockMathExpectedCounter(wordsLengths, keys)) / wordsSum;
        var disperse = forkJoinPool.invoke(new BlockDisperseCounter(wordsLengths, keys, mathExpected)) / wordsSum;

        return new RandomVariableCharacteristics(wordsLengths, mathExpected, disperse, Math.sqrt(disperse));
    }

    public RandomVariableCharacteristics countWordsLengthInSingleThread(Document document) {
        Hashtable<Integer, Integer> wordsLengths = new Hashtable<>();

        for (String line : document.getLines()) {
            for (String word : Helper.wordsIn(line)) {
                Integer count = wordsLengths.getOrDefault(word.length(), 0);
                wordsLengths.put(word.length(), ++count);
            }
        }

        int wordsSum = wordsLengths.values().stream().mapToInt(i -> i).sum();
        final double mathExpected = wordsLengths.entrySet().stream() //sum((xi * ni) / N
                .mapToDouble(i -> (double) i.getKey() * i.getValue()).sum() / wordsSum;
        final double disperse = wordsLengths.entrySet().stream() // sum((xi - xm)^2 * ni) / N
                .mapToDouble(i -> Math.pow((i.getKey() - mathExpected), 2) * i.getValue()).sum() / wordsSum;

        return new RandomVariableCharacteristics(wordsLengths, mathExpected, disperse, Math.sqrt(disperse));
    }

    //math expected
    private class BlockMathExpectedCounter extends RecursiveTask<Double>{

        private static final int BLOCK_SIZE = 2;
        private final Hashtable<Integer, Integer> wordsLengths;
        private final int[] keys;

        public BlockMathExpectedCounter(Hashtable<Integer, Integer> wordsLengths, int[] keys) {
            this.wordsLengths = wordsLengths;
            this.keys = keys;
        }

        @Override
        protected Double compute() {
            if (keys.length > BLOCK_SIZE){
                var task1 = new BlockMathExpectedCounter(wordsLengths, Arrays.copyOfRange(keys, 0, keys.length / 2));
                var task2 = new BlockMathExpectedCounter(wordsLengths, Arrays.copyOfRange(keys, keys.length / 2, keys.length));
                task1.fork();
                task2.fork();
                var res1 = task1.join();
                var res2 = task2.join();
                return res1 + res2;
            } else {
                double sum = 0;
                for (var key : keys){
                    sum += key * wordsLengths.get(key);
                }
                return sum;
            }
        }
    }

    private class BlockDisperseCounter extends RecursiveTask<Double>{

        private static final int BLOCK_SIZE = 2;
        private final double mathExpected;
        private final Hashtable<Integer, Integer> wordsLengths;
        private final int[] keys;

        public BlockDisperseCounter(Hashtable<Integer, Integer> wordsLengths, int[] keys, double mathExpected) {
            this.wordsLengths = wordsLengths;
            this.keys = keys;
            this.mathExpected = mathExpected;
        }

        @Override
        protected Double compute() {
            if (keys.length > BLOCK_SIZE){
                var task1 = new BlockDisperseCounter(wordsLengths, Arrays.copyOfRange(keys, 0, keys.length / 2), mathExpected);
                var task2 = new BlockDisperseCounter(wordsLengths, Arrays.copyOfRange(keys, keys.length / 2, keys.length), mathExpected);
                task1.fork();
                task2.fork();
                var res1 = task1.join();
                var res2 = task2.join();
                return res1 + res2;
            } else {
                double sum = 0;
                for (var key : keys){
                    sum += Math.pow(key - mathExpected, 2) * wordsLengths.get(key);
                }
                return sum;
            }
        }
    }

    private static class BlockWordsCounterTask extends RecursiveTask<Hashtable<Integer, Integer>> {

        private static final int BLOCK_SIZE = 100;
        private final String[] lines;

        public BlockWordsCounterTask(String[] lines) {
            this.lines = lines;
        }

        @Override
        protected Hashtable<Integer, Integer> compute() {
            if (lines.length > BLOCK_SIZE){
                String[] firstBlock = Arrays.copyOfRange(lines, 0, lines.length / 2);
                String[] secondBlock = Arrays.copyOfRange(lines, lines.length / 2, lines.length);

                var task1 = new BlockWordsCounterTask(firstBlock);
                var task2 = new BlockWordsCounterTask(secondBlock);

                task1.fork();
                task2.fork();

                var res1 = task1.join();
                var res2 = task2.join();

                for (var entry : res2.entrySet()){
                    var value = res1.getOrDefault(entry.getKey(), 0);
                    res1.put(entry.getKey(), entry.getValue() + value);
                }

                return res1;
            } else {
                Hashtable<Integer, Integer> wordsLengths = new Hashtable<>();
                for (String line : lines) {
                    for (String word : Helper.wordsIn(line)) {
                        Integer count = wordsLengths.getOrDefault(word.length(), 0);
                        wordsLengths.put(word.length(), ++count);
                    }
                }

                return wordsLengths;
            }
        }
    }


    private static class BlockWordsCounterAction extends RecursiveAction {

        private static final int BLOCK_SIZE = 100;
        private final String[] lines;
        private final Hashtable<Integer, Integer> wordsLengths;

        public BlockWordsCounterAction(String[] lines, Hashtable<Integer, Integer> words) {
            this.lines = lines;
            this.wordsLengths = words;
        }

        @Override
        protected void compute() {
            if (lines.length > BLOCK_SIZE){
                String[] firstBlock = Arrays.copyOfRange(lines, 0, lines.length / 2);
                String[] secondBlock = Arrays.copyOfRange(lines, lines.length / 2, lines.length);

                var task1 = new BlockWordsCounterAction(firstBlock, wordsLengths);
                var task2 = new BlockWordsCounterAction(secondBlock, wordsLengths);

                task1.fork();
                task2.fork();

                task1.join();
                task2.join();
            } else {

                for (String line : lines) {
                    for (String word : Helper.wordsIn(line)) {
                        Integer count = wordsLengths.getOrDefault(word.length(), 0);
                        wordsLengths.put(word.length(), ++count);
                    }
                }
            }
        }
    }

    private class LineWordsCounterTask extends RecursiveAction {

        private final String line;
        private final Hashtable<Integer, Integer> wordsLengths;

        public LineWordsCounterTask(Hashtable<Integer, Integer> wordsLengths,
                                    String line) {
            this.wordsLengths = wordsLengths;
            this.line = line;
        }

        @Override
        protected void compute() {
            for (String word : Helper.wordsIn(line)){
                Integer count = wordsLengths.getOrDefault(word.length(), 0);
                wordsLengths.put(word.length(), ++count);
            }
        }
    }
}
