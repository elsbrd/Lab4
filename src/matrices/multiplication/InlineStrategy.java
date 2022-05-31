package matrices.multiplication;

public class InlineStrategy {
    public Result multiply(int[][] first, int[][] second){
        int[][] matrix = new int[first.length][second[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                matrix[i][j] = 0;
                for (int k = 0; k < second.length; k++) {
                    matrix[i][j] += first[i][k] * second[k][j];
                }
            }
        }
        return new Result(matrix);
    }
}
