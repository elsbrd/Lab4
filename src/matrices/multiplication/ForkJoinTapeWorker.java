package matrices.multiplication;

import java.util.concurrent.RecursiveAction;

public class ForkJoinTapeWorker extends RecursiveAction {
    private final int[][] firstMatrix;
    private final int[][] secondMatrix;
    private final int[][] result;
    private final ImmutableParameters parameters;
    private final int fromRow;
    private final int toRow;
    private final int columnShift;
    public ForkJoinTapeWorker(int fromRow, int toRow, ImmutableParameters parameters) {
        this.fromRow = fromRow;
        this.toRow = toRow;
        this.columnShift = parameters.getColumnShift();
        this.firstMatrix = parameters.getFirstMatrix();
        this.secondMatrix = parameters.getSecondMatrix();
        this.result = parameters.getResult();
        this.parameters = parameters;
    }
    private int getCorrectColumn(int row){
        return (row + columnShift) % result[0].length;
    }
    private int multiplyRowAndColumn(int row, int column){
        int result = 0;
        for (int i = 0; i < firstMatrix[0].length; i++) {
            result += firstMatrix[row][i] * secondMatrix[i][column];
        }
        return result;
    }
    @Override
    protected void compute() {
        int difference = toRow - fromRow;
        if (difference > 0){
            int midRow = fromRow + (difference / 2);
            var task1 = new ForkJoinTapeWorker(fromRow, midRow, parameters);
            var task2 = new ForkJoinTapeWorker(midRow + 1, toRow, parameters);
            task1.fork();
            task2.fork();
            task1.join();
            task2.join();
        } else {
            int row = toRow;
            int column = getCorrectColumn(row);
            result[row][column] = multiplyRowAndColumn(row, column);
        }
    }
}
