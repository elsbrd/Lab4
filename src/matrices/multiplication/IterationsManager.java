package matrices.multiplication;

public class IterationsManager {
    public Result iterate(MultiplicationStrategy strategy){
        for (int i = 0; i < strategy.getIterationsCount(); i++) {
            strategy.nextIteration();
        }
        return strategy.getResult();
    }
}
