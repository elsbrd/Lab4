package common.words;

import example.Document;
import example.Helper;
import word.counter.WordLengthCounter;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SameWordsFinder {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public Set<String> findSameWords(Document first, Document second) {

        Result result = forkJoinPool.invoke(new BlockSameWordsFinder(
                first.getLines().toArray(new String[0]),
                second.getLines().toArray(new String[0])));

        return result.sameWords;
    }

    private static class Result {
        public Set<String> firstTextWords;
        public Set<String> secondTextWords;
        public Set<String> sameWords;

        public Result(Set<String> firstTextWords, Set<String> secondTextWords, Set<String> sameWords) {
            this.firstTextWords = firstTextWords;
            this.secondTextWords = secondTextWords;
            this.sameWords = sameWords;
        }
    }

    private class BlockSameWordsFinder extends RecursiveTask<Result> {

        private static final int BLOCK_SIZE = 200;
        private final String[] firstTextLines;
        private final String[] secondTextLines;

        public BlockSameWordsFinder(String[] firstTextLines, String[] secondTextLines) {
            this.firstTextLines = firstTextLines;
            this.secondTextLines = secondTextLines;
        }

        @Override
        protected Result compute() {
            if (firstTextLines.length > BLOCK_SIZE && secondTextLines.length > BLOCK_SIZE){
                return forkTasks(Arrays.copyOfRange(firstTextLines, 0, firstTextLines.length / 2),
                        Arrays.copyOfRange(firstTextLines, firstTextLines.length / 2, firstTextLines.length),
                        Arrays.copyOfRange(secondTextLines, 0, secondTextLines.length / 2),
                        Arrays.copyOfRange(secondTextLines, secondTextLines.length / 2, secondTextLines.length));
            } else if (firstTextLines.length > BLOCK_SIZE){
                return forkTasks(Arrays.copyOfRange(firstTextLines, 0, firstTextLines.length / 2),
                        Arrays.copyOfRange(firstTextLines, firstTextLines.length / 2, firstTextLines.length),
                        secondTextLines,
                        secondTextLines);
            } else if (secondTextLines.length > BLOCK_SIZE){
                return forkTasks(firstTextLines,
                        firstTextLines,
                        Arrays.copyOfRange(secondTextLines, 0, secondTextLines.length / 2),
                        Arrays.copyOfRange(secondTextLines, secondTextLines.length / 2, secondTextLines.length));
            } else {
                Set<String> firstTextWords = ConcurrentHashMap.newKeySet();
                Set<String> secondTextWords = ConcurrentHashMap.newKeySet();

                for (String line : firstTextLines) {
                    firstTextWords.addAll(Arrays.asList(Helper.wordsIn(line)));
                }

                for (String line : secondTextLines) {
                    secondTextWords.addAll(Arrays.asList(Helper.wordsIn(line)));
                }

                Set<String> result = ConcurrentHashMap.newKeySet();
                result.addAll(firstTextWords);
                result.retainAll(secondTextWords);

                firstTextWords.removeAll(result);
                secondTextWords.removeAll(result);

                return new Result(firstTextWords, secondTextWords, result);
            }
        }

        private Result forkTasks(String[] firstTextFirstBlock,
                                  String[] firstTextSecondBlock,
                                  String[] secondTextFirstBlock,
                                  String[] secondTextSecondBlock){
            var task1 = new BlockSameWordsFinder(firstTextFirstBlock, secondTextFirstBlock);
            var task2 = new BlockSameWordsFinder(firstTextSecondBlock, secondTextSecondBlock);

            task1.fork();
            task2.fork();

            var result1 = task1.join();
            var result2 = task2.join();

            return merge(result1, result2);
        }

        private Result merge(Result r1, Result r2){
            Set<String> firstResult = ConcurrentHashMap.newKeySet();
            Set<String> secondResult = ConcurrentHashMap.newKeySet();

            firstResult.addAll(r1.firstTextWords);
            firstResult.retainAll(r2.secondTextWords);

            secondResult.addAll(r2.firstTextWords);
            secondResult.retainAll(r1.secondTextWords);

            r1.firstTextWords.addAll(r2.firstTextWords);
            r1.secondTextWords.addAll(r2.secondTextWords);

            r1.sameWords.addAll(r2.sameWords);
            r1.sameWords.addAll(firstResult);
            r1.sameWords.addAll(secondResult);

            return r1;
        }
    }
}
