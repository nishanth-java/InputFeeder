package input;

import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.examples.CollectionsConstant;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.util.CsvFileFinder;
import com.ziroh.infinity.util.CsvReader;
import com.ziroh.infinity.util.MathOps;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Test {

    public static void main(String[] args) throws IOException {

        int noOfLayers = 2;
        String vertexId = "K0";
        int weightExclusiveEndRowIdx = 128;
        int weightStartRowIdx  = 64;

        String csvDirectory = "C:/JAVAJAR/weights_csv";
        String saveDir = "C:/Users/nisha/Documents/VertexDemo/VertexDemo/src/main/java/inputfeeder/output.txt";
        for (int layer = 0; layer < noOfLayers; layer++) {
            File weightFile = new File(
                    csvDirectory,
                    "weight_" + layer + "_" + vertexId + ".csv"
            );
            double[][] weightMatrix = CsvReader.readMatrix(weightFile, weightStartRowIdx, weightExclusiveEndRowIdx-1);
            weightMatrix = MathOps.transpose(weightMatrix);
            printMatrix(weightMatrix,saveDir);
        }

    }

    public static void printMatrix(double[][] matrix, String filePath) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath))) {
            out.print("[");

            for (int i = 0; i < matrix.length; i++) {
                out.print(Arrays.toString(matrix[i]));

                if (i < matrix.length - 1) {
                    out.print(",");
                }
            }

            out.println("]");
        }
    }

}