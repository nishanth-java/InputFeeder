package util;

public class BF16MatMul {

    // ==========================================================
    // MMUL: A (m x k) @ B (k x n) = result (m x n)
    //
    // Same semantics as processWeightMatrix():
    //   - every input value is quantized to bf16 before use
    //   - every individual product is quantized to bf16
    //   - the running sum per output cell stays fp32-precision
    //   - the final result is quantized to bf16 once more
    // ==========================================================

    public static double[][] mmul(double[][] a, double[][] b) {

        int m = a.length;          // rows of A
        int k = a[0].length;       // cols of A == rows of B
        int kCheck = b.length;
        int n = b[0].length;       // cols of B

        if (k != kCheck) {
            throw new IllegalArgumentException(
                    "Dimension mismatch: A is " + m + "x" + k +
                            ", B is " + kCheck + "x" + n
            );
        }

        // ------------------------------------------------------
        // Quantize A and B to bf16 up front (mirrors how the
        // original code bf16-rounds delta_a10 and each weight
        // row before multiplying)
        // ------------------------------------------------------

        float[][] aBF16 = new float[m][k];
        for (int i = 0; i < m; i++) {
            for (int p = 0; p < k; p++) {
                aBF16[i][p] = bf16ToFloat(floatToBF16((float) a[i][p]));
            }
        }

        float[][] bBF16 = new float[k][n];
        for (int p = 0; p < k; p++) {
            for (int j = 0; j < n; j++) {
                bBF16[p][j] = bf16ToFloat(floatToBF16((float) b[p][j]));
            }
        }

        // ------------------------------------------------------
        // fp32 accumulator, same as result[i][j] += product
        // in the original
        // ------------------------------------------------------

        float[][] result = new float[m][n];

        for (int p = 0; p < k; p++) {              // contracted dimension (was "weightRows")
            for (int i = 0; i < m; i++) {
                float aVal = aBF16[i][p];           // delta_a10[i][k] equivalent

                for (int j = 0; j < n; j++) {
                    float product = aVal * bBF16[p][j];              // A[i][p] * B[p][j]
                    product = bf16ToFloat(floatToBF16(product));      // quantize product

                    result[i][j] += product;         // fp32 accumulation
                }
            }
        }

        // ------------------------------------------------------
        // Final bf16 rounding pass, same as the original's
        // "Applying final BF16 rounding..." step
        // ------------------------------------------------------

        double[][] out = new double[m][n];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                out[i][j] = bf16ToFloat(floatToBF16(result[i][j]));
            }
        }

        return out;
    }

    // ==========================================================
    // FLOAT -> BF16 (round-to-nearest-even)
    // ==========================================================

    private static short floatToBF16(float value) {
        int bits = Float.floatToRawIntBits(value);
        int lower = bits & 0xFFFF;
        int upper = bits >>> 16;

        if (lower > 0x8000 || (lower == 0x8000 && (upper & 1) != 0)) {
            upper++;
        }

        return (short) (upper & 0xFFFF);
    }

    // ==========================================================
    // BF16 -> FLOAT
    // ==========================================================

    private static float bf16ToFloat(short bf16) {
        int bits = (bf16 & 0xFFFF) << 16;
        return Float.intBitsToFloat(bits);
    }
}