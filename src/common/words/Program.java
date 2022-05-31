package common.words;

import example.Document;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Program {
    public static void main(String[] args) throws IOException {
        File file = new File("src\\common\\words\\Harry Potter 1.txt");
        Document harryPotter = Document.fromFile(file);
        File file2 = new File("src\\common\\words\\The-Hobbit.txt");
        Document theHobbit = Document.fromFile(file2);

        SameWordsFinder finder = new SameWordsFinder();

        Set<String> result = finder.findSameWords(harryPotter, theHobbit);

        System.out.println(result.size());
        for (var item : result) {
            System.out.println(item);
        }
    }
}
