package test;

import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;

public class MmulTest {
    public static double[][] mmul(double[][] matrix1, double[][] matrix2) {
        if (matrix1 == null || matrix2 == null) {
            throw new IllegalArgumentException("Matrices cannot be null.");
        }
        int rows = matrix1.length;
        int k = matrix1[0].length;
        int k2 = matrix2.length;
        int cols = matrix2[0].length;
        if (k != k2) {
            throw new IllegalArgumentException(
                    "Dimension mismatch: matrix1 cols (" + k + ") != matrix2 rows (" + k2 + ")");
        }
        try (NDManager manager = NDManager.newBaseManager();) {
            NDArray a = manager.create(MmulTest.flatten(matrix1), new Shape(rows, k));
            NDArray b = manager.create(MmulTest.flatten(matrix2), new Shape(k, cols));
            NDArray aBf16 = a.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);
            NDArray bBf16 = b.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);
            NDArray productF32 = aBf16.matMul(bBf16);
            NDArray productBf16 = productF32.toType(DataType.BFLOAT16, false).toType(
                    DataType.FLOAT32, false);
            float[] resultFlat = productBf16.toFloatArray();
            double[][] result = new double[rows][cols];
            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < cols; ++c) {
                    result[r][c] = resultFlat[r * cols + c];
                }
            }
            double[][] dArray = result;
            return dArray;
        }
    }

    private static float[] flatten(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        float[] flat = new float[rows * cols];
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < cols; ++c) {
                flat[r * cols + c] = (float) matrix[r][c];
            }
        }
        return flat;
    }

    public static void main(String[] args) {
        double[][] mat1 = {
                {2.5, -1.0,  3.0},
                {4.0,  2.0, -2.5},
                {-1.5, 5.0,  1.0}
        };

        double[][] mat2 = {
                {1.0,  3.0, -2.0},
                {-4.0, 2.5,  1.0},
                {3.0, -1.5,  4.0}
        };

        double[][] result = mmul(mat1, mat2);

        for (int i = 0; i < result.length; i++) {
            for (int j = 0; j < result[i].length; j++) {
                System.out.print(result[i][j] + " ");
            }
            System.out.println();
        }

    }
}
