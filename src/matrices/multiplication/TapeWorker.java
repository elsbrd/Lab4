package matrices.multiplication;

public class TapeWorker implements Runnable {
    private int column;

    private final int row;
    private final int[][] first;
    private final int[][] second;
    private final int[][] result;

    public TapeWorker(int row, int column, int[][] first, int[][] second, int[][] result) {
        this.row = row;
        this.column = column;
        this.first = first;
        this.second = second;
        this.result = result;
    }

    @Override
    public void run(){
        result[row][column] = multiplyRowAndColumn(row, column);
        column = (column + 1) % result[0].length;
    }

    private int multiplyRowAndColumn(int row, int column){
        int result = 0;
        for (int i = 0; i < first[0].length; i++) {
            result += first[row][i] * second[i][column];
        }
        return result;
    }
}
