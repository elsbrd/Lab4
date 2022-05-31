package documents;

import example.Document;
import example.Folder;
import example.Helper;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FilesBrowser {
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    public List<String> findFilesByTheme(Folder folder, Set<String> themeWords){
        return forkJoinPool.invoke(new FolderSearchTask(folder, themeWords));
    }

    class FolderSearchTask extends RecursiveTask<List<String>> {
        private final Folder folder;
        private final Set<String> searchedWords;

        public FolderSearchTask(Folder folder, Set<String> searchedWords) {
            this.folder = folder;
            this.searchedWords = searchedWords;
        }

        @Override
        protected List<String> compute() {
            List<String> files = new LinkedList<>();
            List<RecursiveTask<List<String>>> tasks = new LinkedList<>();

            for (Folder folder : folder.getSubFolders()) {
                FolderSearchTask task = new FolderSearchTask(folder, searchedWords);
                tasks.add(task);
                task.fork();
            }

            for (Document document : folder.getDocuments()) {
                DocumentSearchTask task = new DocumentSearchTask(document, searchedWords);
                tasks.add(task);
                task.fork();
            }

            for (RecursiveTask<List<String>> task : tasks) {
                List<String> result = task.join();
                if (result != null){
                    files.addAll(result);
                }
            }

            return files;
        }
    }

    class DocumentSearchTask extends RecursiveTask<List<String>> {
        private final Document document;
        private final Set<String> searchedWords;

        public DocumentSearchTask(Document document, Set<String> searchedWords) {
            this.document = document;
            this.searchedWords = searchedWords;
        }

        @Override
        protected List<String> compute() {

            for (String line : document.getLines()) {
                for (String word : Helper.wordsIn(line)) {
                    if (searchedWords.contains(word.toLowerCase(Locale.ROOT))){
                        return Collections.singletonList(document.getPath());
                    }
                }
            }
            return null;
        }
    }

    class BlockSearchTask extends RecursiveTask<Boolean> {

        private final int BLOCK_SIZE = 100;
        private final String[] lines;
        private final Set<String> searchWords;

        public BlockSearchTask(String[] lines, Set<String> searchWords) {
            this.lines = lines;
            this.searchWords = searchWords;
        }

        @Override
        protected Boolean compute() {
            if (lines.length > BLOCK_SIZE){
                String[] firstBlock = Arrays.copyOfRange(lines, 0, lines.length / 2);
                String[] secondBlock = Arrays.copyOfRange(lines, lines.length / 2, lines.length);

                var task1 = new BlockSearchTask(firstBlock, searchWords);
                var task2 = new BlockSearchTask(secondBlock, searchWords);

                task1.fork();
                task2.fork();

                return task1.join() || task2.join();
            } else {
                for (String line : lines) {
                    for (String word : Helper.wordsIn(line)) {
                        if (searchWords.contains(word)){
                            return true;
                        }
                    }
                }
                return false;
            }
        }
    }
}
