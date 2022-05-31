package documents;

import example.Folder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Program {
    public static void main(String[] args) throws IOException {
        Folder folder = Folder.fromDirectory(new File("src/common/words"));
        FilesBrowser browser = new FilesBrowser();

        Set<String> searchedWords = ConcurrentHashMap.newKeySet();
        searchedWords.addAll(Arrays.asList("elf","Harry"));

        List<String> result = browser.findFilesByTheme(folder, searchedWords);

        for (String file : result){
            System.out.println(file);
        }
    }
}
