package matrices.multiplication;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TapeStrategy implements MultiplicationStrategy {
    private final int threadsCount;
    private Collection<TapeWorker> workers;
    private Result result;


    private final int[][] first;
    private final int[][] second;

    public TapeStrategy(int[][] first, int[][] second, int threadsCount) {
        this.first = first;
        this.second = second;
        this.threadsCount = threadsCount;
        result = new Result(new int[first.length][second[0].length]);
        workers = new ArrayList<>(second.length);
        prepareWorkers();
    }

    private void prepareWorkers(){
        for (int i = 0; i < result.getMatrix().length; i++) {
            workers.add(new TapeWorker(i, i, first, second, result.getMatrix()));
        }
    }

    public void nextIteration() {
        ExecutorService executor = Executors.newFixedThreadPool(threadsCount);
        for (TapeWorker worker : workers) {
            executor.execute(worker);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Result getResult(){
        return result;
    }

    public int getIterationsCount(){
        return first[0].length;
    }
}
