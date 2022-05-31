package matrices.multiplication;

public interface MultiplicationStrategy {
    int getIterationsCount();
    void nextIteration();
    Result getResult();
}
