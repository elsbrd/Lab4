package matrices.multiplication;

public class ImmutableParameters {
    private final int columnShift;
    private final int[][] firstMatrix;
    private final int[][] secondMatrix;
    private final int[][] result;

    public ImmutableParameters(int columnShift, int[][] firstMatrix, int[][] secondMatrix, int[][] result) {
        this.columnShift = columnShift;
        this.firstMatrix = firstMatrix;
        this.secondMatrix = secondMatrix;
        this.result = result;
    }

    public int getColumnShift() {
        return columnShift;
    }

    public int[][] getFirstMatrix() {
        return firstMatrix;
    }

    public int[][] getSecondMatrix() {
        return secondMatrix;
    }

    public int[][] getResult() {
        return result;
    }
}
