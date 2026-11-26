package util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class UtilHelper {
    /**
     * Reads a Json file and returns a matrix. Pass null and it treats the json as having [[]] without any variables
     * @param file File
     * @param variable variable if present, pass null if no variable in json
     * @return double[][]
     */
    public static double[][] readJsonMatrix(File file, String variable) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(file);

            JsonNode matrixNode;

            if (variable == null) {
                // JSON itself is the matrix
                matrixNode = root;
            } else {
                // JSON contains the matrix under variable -> values
                matrixNode = root.get(variable).get("values");
            }
            return mapper.treeToValue(matrixNode, double[][].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves a double[][] into csv
     * @param matrix double[][]
     * @param file File
     */
    public static void saveMatrixToCsv(double[][] matrix, File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {

            for (double[] row : matrix) {
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) {
                        writer.write(",");
                    }
                    writer.write(Double.toString(row[i]));
                }
                writer.newLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Saves a double[][] matrix into the specified file as json
     * @param matrix double[][] matrix
     * @param file File file
     */
    public static void saveMatrixToJson(double[][] matrix, File file)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, matrix);
        }catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
