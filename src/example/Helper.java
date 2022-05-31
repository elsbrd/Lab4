package example;

public class Helper {
    public static String[] wordsIn(String line) {
        return line.trim().split("(\\s|\\p{Punct})+");
    }
}
