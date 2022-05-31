package matrices.multiplication;

import java.util.concurrent.ForkJoinPool;

public class ForkJoinTapeStrategy implements MultiplicationStrategy {
    private final Result result;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private final int[][] first;
    private final int[][] second;

    private int columnShift = 0;

    public ForkJoinTapeStrategy(int[][] first, int[][] second) {
        this.first = first;
        this.second = second;
        result = new Result(new int[first.length][second[0].length]);
    }

    public void nextIteration() {
        var parameters = new ImmutableParameters(columnShift, first, second, result.getMatrix());
        forkJoinPool.invoke(new ForkJoinTapeWorker(0, first.length - 1, parameters));
        columnShift++;
    }

    public Result getResult(){
        return result;
    }

    public int getIterationsCount(){
        return first[0].length;
    }
}
