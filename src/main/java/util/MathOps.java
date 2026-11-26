package util;

import com.ziroh.infinity.util.MatrixValidator;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;

public class MathOps {

    public static double[][] mmul(double[][] matrix1, double[][] matrix2) {
        if (matrix1 == null || matrix2 == null) {
            throw new IllegalArgumentException("Matrices cannot be null.");
        }
        int rows = matrix1.length;
        int k = matrix1[0].length;
        int k2 = matrix2.length;
        int cols = matrix2[0].length;
        if (k != k2) {
            throw new IllegalArgumentException("Dimension mismatch: matrix1 cols (" + k + ") != matrix2 rows (" + k2 + ")");
        }

        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray a = manager.create(flatten(matrix1), new Shape(rows, k));
            NDArray b = manager.create(flatten(matrix2), new Shape(k, cols));

            NDArray aBf16 = a.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);
            NDArray bBf16 = b.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);

            NDArray productF32 = aBf16.matMul(bBf16);
            NDArray productBf16 = productF32.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);

            float[] resultFlat = productBf16.toFloatArray();
            double[][] result = new double[rows][cols];
            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < cols; ++c) {
                    result[r][c] = resultFlat[r * cols + c];
                }
            }
            return result;
        }
    }

    public static double[][] transpose(double[][] matrix) {
        MatrixValidator.validateMatrix(matrix);
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; ++i) {
            double[] rowRef = matrix[i];
            for (int j = 0; j < cols; ++j) {
                result[j][i] = rowRef[j];
            }
        }
        return result;
    }

    public static double[][] divideByScalarInPlace(double[][] matrix, double value) {
        if (matrix == null || matrix.length == 0) {
            throw new IllegalArgumentException("Matrix cannot be null or empty.");
        }
        if (value == 0.0) {
            throw new IllegalArgumentException("Division by zero");
        }
        int rows = matrix.length;
        int cols = matrix[0].length;

        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray m = manager.create(flatten(matrix), new Shape(rows, cols));
            NDArray mBf16 = m.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);

            NDArray valueArr = manager.create(new float[]{(float) value});
            NDArray valueBf16Arr = valueArr.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);
            float valueBf16 = valueBf16Arr.toFloatArray()[0];

            NDArray quotient = mBf16.mul(1.0f / valueBf16);
            NDArray quotientBf16 = quotient.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);

            float[] resultFlat = quotientBf16.toFloatArray();
            double[][] result = new double[rows][cols];
            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < cols; ++c) {
                    result[r][c] = resultFlat[r * cols + c];
                }
            }
            return result;
        }
    }

    public static double[][] add(double[][] matrix1, double[][] matrix2) {
        if (matrix1 == null || matrix2 == null) {
            throw new IllegalArgumentException("Matrices cannot be null.");
        }
        if (matrix1.length != matrix2.length || matrix1[0].length != matrix2[0].length) {
            throw new IllegalArgumentException("Shape mismatch: matrix1 is " + matrix1.length + "x" + matrix1[0].length + ", matrix2 is " + matrix2.length + "x" + matrix2[0].length);
        }
        int rows = matrix1.length;
        int cols = matrix1[0].length;

        try (NDManager manager = NDManager.newBaseManager()) {
            NDArray a = manager.create(flatten(matrix1), new Shape(rows, cols));
            NDArray b = manager.create(flatten(matrix2), new Shape(rows, cols));

            NDArray aBf16 = a.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);
            NDArray bBf16 = b.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);

            NDArray sum = aBf16.add(bBf16);
            NDArray sumBf16 = sum.toType(DataType.BFLOAT16, false).toType(DataType.FLOAT32, false);

            float[] resultFlat = sumBf16.toFloatArray();
            double[][] result = new double[rows][cols];
            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < cols; ++c) {
                    result[r][c] = resultFlat[r * cols + c];
                }
            }
            return result;
        }
    }

    public static double[][] sgd(double[][] weight, double[][] gradient, double learningRate) {
        int rows = weight.length;
        int cols = weight[0].length;
        double[][] updatedWeight = new double[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                updatedWeight[i][j] = weight[i][j] - learningRate * gradient[i][j];
            }
        }
        return updatedWeight;
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

}
