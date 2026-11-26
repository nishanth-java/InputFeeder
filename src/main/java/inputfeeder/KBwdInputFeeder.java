package inputfeeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.IndexedMatrixBatch;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.MatrixDoubleSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class KBwdInputFeeder {

    private static final String HOST = "localhost";
    private static final int PORT = 20001;      // V0
    private static final String VERTEX_ID = "K0";

    private static final int FIRST_Q = 0;
    private static final int LAST_Q = 6;

    public static void main(String[] args) {

        Thread inputThread = new Thread(() -> {
            try {
                VertexHttpClient client = new VertexHttpClient();
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(new File(
                        "D:/Infinity/Local/InputFiles/delta_ki.json"));

                for (int qNum = FIRST_Q; qNum <= LAST_Q; qNum++) {
                    sendQ(root, client, qNum);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        inputThread.start();
    }

    /**
     * Builds and sends the InfinityMessage payload for a single Q_n block.
     */
    private static void sendQ(JsonNode root, VertexHttpClient client, int qNum) throws Exception {
        String qKey = "Q_" + qNum;

        double[][] mat = toMatrix(root.get(qKey).get("values"));

        List<IndexedMatrix> inputList = new ArrayList<>();
        inputList.add(new IndexedMatrix(0, mat));
        List<IndexedMatrixBatch>  batchList = new ArrayList<>();
        batchList.add(0, new IndexedMatrixBatch(0,inputList));

        InfinityMessage infinityMessage = MatrixDoubleSequenceBatchBlockMessage.createInfinityMessage(
                String.valueOf(1), "source1", "source2", "edge1", "delta_ki$7", batchList);

        InfinityMessageWrapper wrapper = new InfinityMessageWrapper(infinityMessage);

        MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
        mu.setKey("K5-" + "2.0.111-");
        mu.setValue(wrapper);

        String json = new Helper().getJson(mu);
        System.out.println(qKey + " payload: " + json);

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