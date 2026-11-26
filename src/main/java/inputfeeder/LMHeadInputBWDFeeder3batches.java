package inputfeeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class LMHeadInputBWDFeeder3batches {

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
                JsonNode root = mapper.readTree(new File("C:\\Users\\nisha\\Downloads\\a10_delta_a10_b0_builtin (1).json"));

                JsonNode values = root.get("tensors").get("delta_a10_b0").get("data");

                JsonNode root1 = mapper.readTree(new File("C:\\Users\\nisha\\Downloads\\a10_delta_a10_b1_builtin.json"));

                JsonNode values1 = root1.get("tensors").get("delta_a10_b1").get("data");

                JsonNode root2 = mapper.readTree(new File("C:\\Users\\nisha\\Downloads\\a10_delta_a10_b2_builtin.json"));

                JsonNode values2 = root2.get("tensors").get("delta_a10_b2").get("data");

                double[][] mat = toMatrix(values);
                double[][] mat1 = toMatrix(values1);
                double[][] mat2 = toMatrix(values2);

                List<IndexedMatrix> inputList = new ArrayList<>();

                inputList.add(new IndexedMatrix(0, mat));
                inputList.add(new IndexedMatrix(1, mat1));
                inputList.add(new IndexedMatrix(2, mat2));

                InfinityMessage msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                        "source2","edge1", "delta_gt", inputList);

                InfinityMessageWrapper input1 = new InfinityMessageWrapper(msg);
                MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
                mu.setKey("K5-" + "2.0.111-");
                mu.setValue(input1);

                String json = new Helper().getJson(mu);

                System.out.println(json);
                URI uri = URI.create(
                        "http://localhost:20304/input?vertexid=LMHead"
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
