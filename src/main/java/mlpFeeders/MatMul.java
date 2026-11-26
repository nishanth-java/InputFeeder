package mlpFeeders;

import ai.djl.engine.Engine;

import ai.djl.Device;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MatMul {


    private static final String MATRIX1_PATH =
            "C:\\Users\\vsari\\Downloads\\Sept4_lm_head_delta_a10_b0.json";

    private static final String MATRIX2_PATH =
            "C:\\Infinity_files\\weights_csv\\weight_LMHead.csv";


    private static final int MATRIX1_ROWS = 4;
    private static final int MATRIX1_COLS = 151936;

    private static final int MATRIX2_ROWS = 151936;
    private static final int MATRIX2_COLS = 896;


    private static void collectNumbers(
            JsonNode node,
            List<Double> values) {


        if (node == null) {
            return;
        }


        // ======================================================
        // NUMBER
        // ======================================================

        if (node.isNumber()) {

            values.add(
                    node.asDouble()
            );

            return;
        }

    }
        private static double[][] loadMatrix1Json(
            String filePath)
            throws IOException {


        Path path =
                Paths.get(filePath);


        if (!Files.exists(path)) {

            throw new IOException(
                    "Matrix 1 JSON not found:\n"
                            + filePath
            );
        }


        ObjectMapper mapper =
                new ObjectMapper();


        JsonNode root =
                mapper.readTree(
                        path.toFile()
                );


        // ======================================================
        // COLLECT ALL NUMBERS
        // ======================================================

        List<Double> values =
                new ArrayList<>();


        collectNumbers(
                root,
                values
        );


        // ======================================================
        // EXPECTED SIZE
        // ======================================================

        int expectedValues =
                MATRIX1_ROWS *
                        MATRIX1_COLS;


        System.out.println(
                "JSON numeric values found = "
                        + values.size()
        );

        System.out.println(
                "Expected numeric values = "
                        + expectedValues
        );


        // ======================================================
        // CHECK SIZE
        // ======================================================

        if (values.size() != expectedValues) {

            throw new IOException(
                    "Invalid Matrix 1 size.\n"
                            + "Found = "
                            + values.size()
                            + "\n"
                            + "Expected = "
                            + expectedValues
                            + "\n"
                            + "Expected shape = "
                            + MATRIX1_ROWS
                            + " x "
                            + MATRIX1_COLS
            );
        }


        // ======================================================
        // CREATE MATRIX
        // ======================================================

        double[][] matrix =
                new double[
                        MATRIX1_ROWS
                        ][
                        MATRIX1_COLS
                        ];


        int index = 0;


        // ======================================================
        // FILL MATRIX
        // ======================================================

        for (int i = 0;
             i < MATRIX1_ROWS;
             i++) {

            for (int j = 0;
                 j < MATRIX1_COLS;
                 j++) {

                matrix[i][j] =
                        values.get(index++);

            }
        }


        return matrix;
    }


    public static double[][] matMulti(double[][] matrix1  , double[][] matrix2){

        try(NDManager manager = NDManager.newBaseManager(Device.cpu())){
            System.out.println("Before creation");
            NDArray mat1 = manager.create(matrix1);
            System.out.println("after creation");
            mat1.toType(DataType.BFLOAT16,false);
            System.out.println("After bf16");
            NDArray mat2 = manager.create(matrix2).toType(DataType.BFLOAT16,false);


            NDArray result = mat1.matMul(mat2);

            long rows = result.getShape().get(0);
            long cols = result.getShape().get(1);

            double[][] output = new double[(int) rows][(int) cols];

            float[] values = result.toFloatArray();

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    output[i][j] = values[i * (int) cols + j];
                }
            }

            return output;
        }

        }


        public boolean test(){

return true;

        }


    public static void main(String[] args) {

        System.out.println("Engine = " + Engine.getInstance().getEngineName());
        System.out.println("Engine version = " + Engine.getInstance().getVersion());
        double[][] mat1 = {{1, 2}, {2, 3}};
        double[][] mat2 = {{2, 2}, {4, 3}};

        double[][] op = matMulti(mat1,mat2);
        for (double[] row : op) {
            System.out.println(java.util.Arrays.toString(row));
        }
    }



    private static float roundBf16(float value) {
        int bits = Float.floatToIntBits(value);
        int rounded = bits + 32767 + (bits >>> 16 & 1);
        int truncated = rounded & -65536;
        return Float.intBitsToFloat(truncated);
    }


    }




