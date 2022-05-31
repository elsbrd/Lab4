package word.counter;

import example.Document;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

public class Program {
    public static void main(String[] args) throws IOException {
        File file = new File("src\\common\\words\\Harry Potter 1.txt");
        Document document = Document.fromFile(file);
        WordLengthCounter counter = new WordLengthCounter();

        long time = System.currentTimeMillis();
        RandomVariableCharacteristics result = counter.countWordsLengthInSingleThread(document);
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        for (var entry : result.wordsLengths.entrySet()) {
            System.out.println("Length " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Math expected: " + result.mathExpected);
        System.out.println("Disperse: " + result.disperse);
        System.out.println("Deviation: " + result.deviation);

        System.out.println();

        time = System.currentTimeMillis();
        result = counter.countWordsLengthInRealParallel(document);
        System.out.println("Time: " + (System.currentTimeMillis() - time));
        for (var entry : result.wordsLengths.entrySet()) {
            System.out.println("Length " + entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Math expected: " + result.mathExpected);
        System.out.println("Disperse: " + result.disperse);
        System.out.println("Deviation: " + result.deviation);
    }
}
