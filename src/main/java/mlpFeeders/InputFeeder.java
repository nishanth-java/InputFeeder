package mlpFeeders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.inputMatrixMessage.InputMatrixMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;
import com.ziroh.infinity.util.MathOps;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class InputFeeder {

    public static void main(String[] args) {

        //----------INPUT THREAD-------//

        Thread inputThread = new Thread(() -> {
            try
            {
                VertexHttpClient client = new VertexHttpClient();

          /*  double [][] mat = new double [5][896];
            for(int i=0;i<5;i++)
            {
                for(int j=0;j<896;j++)
                {
                    mat[i][j]=i+j;
                }
            }
          */
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(new File("C://Users/willi/Downloads/forward_builtin_new_checkpoints.json"));

                JsonNode values = root.get("b0.layer0.gate").get("values");


                double[][] mat = toMatrix(values);


                System.out.println("Matrix from file is => ");
                for (int i = 0; i < mat.length; i++) {
                    for (int j = 0; j < mat[0].length; j++) {
                        System.out.println(mat[i][j]);
                    }
                }

                List<IndexedMatrix> inputList = new ArrayList<>();

                inputList.add(new IndexedMatrix(0, mat));




                InfinityMessage msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                        "source2","edge1", "GATE", inputList);

                InfinityMessageWrapper input1 = new InfinityMessageWrapper(msg);
                MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
                mu.setKey("K5-" + "2.0.111-");
                mu.setValue(input1);

                String json = new Helper().getJson(mu);

                System.out.println(json);
                URI uri = URI.create(
                        "http://localhost:20503/input?vertexid=UP"
                );

                System.out.println("[INPUT] POST -> " + uri);

                client.sendDataSync(uri, json);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        inputThread.start();
    }
    private static double[][] toMatrix(JsonNode valuesNode) {
        double[][] mat = new double[valuesNode.size()][];
        for (int i = 0; i < valuesNode.size(); i++) {
            JsonNode row = valuesNode.get(i);
            mat[i] = new double[row.size()];
            for (int j = 0; j < row.size(); j++) {
                mat[i][j] = row.get(j).asDouble();
            }
        }
        return mat;
    }
}