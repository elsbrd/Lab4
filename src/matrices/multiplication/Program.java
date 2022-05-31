package matrices.multiplication;

import java.util.Random;

public class Program {
    static IterationsManager manager = new IterationsManager();

    public static void main(String[] args) {

        /*int[][] firstMatrix = createMatrix(1000, 1000);
        int[][] secondMatrix = createMatrix(1000, 1000);

        int[][] inlineMatrix = new InlineStrategy().multiply(firstMatrix, secondMatrix).getMatrix();
        int[][] tapeMatrix = manager.iterate(new ForkJoinTapeStrategy(firstMatrix, secondMatrix)).getMatrix();

        System.out.println(matricesEqual(inlineMatrix, tapeMatrix));*/

        int[] sizeTests = { 500, 1000, 1500, 2000, 2500 };

        for (int i = 0; i < sizeTests.length; i++) {
            runSizeTest(sizeTests[i]);
        }
    }

    private static void runSizeTest(int matrixRank){
        System.out.println("Size test: " + matrixRank + " matrix rank");
        int[][] firstMatrix = createMatrix(matrixRank, matrixRank);
        int[][] secondMatrix = createMatrix(matrixRank, matrixRank);
        TapeStrategy tape = new TapeStrategy(firstMatrix, secondMatrix, 8);
        ForkJoinTapeStrategy forkJoinTape = new ForkJoinTapeStrategy(firstMatrix, secondMatrix);

        long start;

        System.out.print("Tape method: ");
        start = System.currentTimeMillis();
        manager.iterate(tape);
        long tapeTime = System.currentTimeMillis() - start;
        System.out.println(tapeTime);

        System.out.print("Fork join tape method: ");
        start = System.currentTimeMillis();
        manager.iterate(forkJoinTape);
        long forkJoinTapeTime = System.currentTimeMillis() - start;
        System.out.println(forkJoinTapeTime);

        System.out.println("Speed up: " + ((double) tapeTime / forkJoinTapeTime));

        System.out.println();
    }

    private static int[][] createMatrix(int rows, int columns){
        int[][] matrix = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                matrix[i][j] = new Random().nextInt(100);
            }
        }
        return matrix;
    }

    private static boolean matricesEqual(int[][] expected, int[][] actual){
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                if (expected[i][j] != actual[i][j]){
                    return false;
                }
            }
        }
        return true;
    }
}
