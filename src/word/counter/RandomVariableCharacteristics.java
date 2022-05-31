package word.counter;

import java.util.Hashtable;

public class RandomVariableCharacteristics {
    public Hashtable<Integer, Integer> wordsLengths;
    public double mathExpected;
    public double disperse;
    public double deviation;

    public RandomVariableCharacteristics(Hashtable<Integer, Integer> wordsLengths,
                                         double mathExpected, double disperse, double deviation) {
        this.wordsLengths = wordsLengths;
        this.mathExpected = mathExpected;
        this.disperse = disperse;
        this.deviation = deviation;
    }
}
