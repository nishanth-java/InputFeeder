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

public class Rms0BwdInputFeederDirectLambdadelta {

    private static final String HOST = "localhost";
    private static final int PORT = 20000;
    private static final String VERTEX_ID = "RMS0";

    public static void main(String[] args) {

        Thread inputThread = new Thread(() -> {
            try {
                VertexHttpClient client = new VertexHttpClient();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(new File(
                        "D:/Infinity/Local/InputFiles/rms0bwdinputs.json"));

                // Replace each category string with the required category type
                sendQ(root, client, 0, "delta_v");
                sendQ(root, client, 1, "delta_v");
                sendQ(root, client, 2, "delta_q");
                sendQ(root, client, 3, "delta_k");
                sendQ(root, client, 4, "delta_k");

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        inputThread.start();
    }

    /**
     * Builds and sends the InfinityMessage payload for a single Q_n block
     * using the given category type.
     */
    private static void sendQ(JsonNode root, VertexHttpClient client, int qNum, String category) throws Exception {
        String qKey = "Q_" + qNum;

        double[][] mat = toMatrix(root.get(qKey).get("values"));

        List<IndexedMatrix> inputList = new ArrayList<>();
        inputList.add(new IndexedMatrix(0, mat));

        InfinityMessage infinityMessage = MatrixSequenceBatchBlockMessage.createInfinityMessage(
                String.valueOf(1), "source1", "source2", "edge1", category, inputList);

        InfinityMessageWrapper wrapper = new InfinityMessageWrapper(infinityMessage);

        MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
        mu.setKey("K5-" + "2.0.111-");
        mu.setValue(wrapper);

        String json = new Helper().getJson(mu);
        System.out.println(qKey + " (" + category + ") payload: " + json);

        URI uri = URI.create("http://" + HOST + ":" + PORT + "/input?vertexid=" + VERTEX_ID);

        System.out.println("[INPUT] POST -> " + uri);
        client.sendDataSync(uri, json);
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