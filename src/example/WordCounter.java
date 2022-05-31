package example;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class WordCounter {
    private final ForkJoinPool forkJoinPool =
            new ForkJoinPool();

    String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }

    Long occurrencesCount(Document document,
                          String searchedWord) {
        long count = 0;
        for (String line : document.getLines()) {
            for (String word : wordsIn(line)) {
                if (searchedWord.equals(word)) {
                    count++;
                }
            }
        }
        return count;
    }

    Long countOccurrencesOnSingleThread( Folder folder,
                                         String searchedWord) {
        long count = 0;
        for (Folder subFolder : folder.getSubFolders()) {
            count = count + countOccurrencesOnSingleThread(
                    subFolder, searchedWord);
        }
        for (Document document : folder.getDocuments()) {
            count = count + occurrencesCount(document,
                    searchedWord);
        }
        return count;
    }

    Long countOccurrencesInParallel(Folder folder,
                                    String searchedWord){
        return forkJoinPool.invoke(
                new FolderSearchTask(folder, searchedWord));
    }

    class DocumentSearchTask extends RecursiveTask<Long> {
        private final Document document;
        private final String searchedWord;

        public DocumentSearchTask(Document document, String searchedWord) {
            this.document = document;
            this.searchedWord = searchedWord;
        }

        @Override
        protected Long compute() {
            return occurrencesCount(document, searchedWord);
        }
    }

    class FolderSearchTask extends RecursiveTask<Long> {
        private final Folder folder;
        private final String searchedWord;

        public FolderSearchTask(Folder folder, String searchedWord) {
            this.folder = folder;
            this.searchedWord = searchedWord;
        }

        @Override
        protected Long compute() {
            long count = 0L;
            List<RecursiveTask<Long>> tasks = new LinkedList<>();

            for (Document document : folder.getDocuments()) {
                DocumentSearchTask task =
                        new DocumentSearchTask(document, searchedWord);
                tasks.add(task);
                task.fork();
            }

            for (RecursiveTask<Long> task : tasks) {
                count = count + task.join();
            }
            return count;
        }
    }

    public static void main(String[] args) throws IOException {
        Folder folder =
                Folder.fromDirectory(new File("D:/TextFolder"));
        WordCounter wordCounter = new WordCounter();
        String searchedWord = "synchronized";
        final int repeatCount = 4;
        long startTime, stopTime, counts=0, averTime = 0;
        for (int i = 0; i < repeatCount; i++) {
            startTime = System.currentTimeMillis();
            counts = wordCounter.countOccurrencesInParallel(folder,
                    searchedWord);
            stopTime = System.currentTimeMillis();
            averTime+=stopTime - startTime;
        }
        System.out.println(counts +
                " words are fined. Fork / join search took " +
                averTime/repeatCount+ "ms");

        averTime = 0;
        for (int i = 0; i < repeatCount; i++) {
            startTime = System.currentTimeMillis();
            counts = wordCounter.countOccurrencesOnSingleThread(folder,searchedWord);
            stopTime = System.currentTimeMillis();
            averTime+=stopTime - startTime;
        }
        System.out.println(counts +
                " words are fined. Single thread search took " +
                averTime/repeatCount+ "ms");
    }

}
