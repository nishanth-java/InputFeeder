package util;

import com.ziroh.infinity.util.CsvReader;

import java.io.File;
import java.io.IOException;

public class TestMatMul {

    public static void main(String[] args) throws IOException {
        File weightFile = new File("D:/Infinity/RemoteServer/weight_LMHead.csv");

        double[][] weightMatrix = CsvReader.readMatrix(weightFile, 0, 151935);
       // double[][] weightMatrixTransposed = MathOps.transpose(weightMatrix);

        File delta_a10 = new File("D:/Infinity/RemoteServer/Sept4_lm_head_delta_a10_b0.json");

        double[][] delta_a10Matrix = UtilHelper.readJsonMatrix(delta_a10, null);

        double[][] result = BF16MatMul.mmul( delta_a10Matrix,weightMatrix);

        File outputFile = new File("D:/Infinity/RemoteServer/lmheadoutputjson.json");

        UtilHelper.saveMatrixToJson(result, outputFile);

    }

}
