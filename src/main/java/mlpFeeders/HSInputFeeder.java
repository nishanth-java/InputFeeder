package mlpFeeders;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.IndexedMatrixBatch;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.MatrixDoubleSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class HSInputFeeder {

    private static final int NUM_HEADS = 14;

    // Change this prefix on every run if the memory store is not cleared,
    // otherwise you will get KeyAlreadyExist again.
    private static final String KEY_PREFIX = "K5-2.0.111-";

    public static void main(String[] args) throws IOException {

        //----------INPUT THREAD-------//
        List<Thread> threads = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(
                new File("C://Users/willi/Downloads/forward_builtin_new_checkpoints.json"));

        // One list per head, loaded from the JSON
        List<List<IndexedMatrix>> matLists = new ArrayList<>();
        for (int h = 0; h < NUM_HEADS; h++) {
            List<IndexedMatrix> list = new ArrayList<>();
            list.add(new IndexedMatrix(0,
                    toMatrix(root.get("b0.layer0.head" + h + ".attn_out").get("values"))));
            matLists.add(list);
        }

        for (int i = 0; i < NUM_HEADS; i++) {
            final int batchSequenceId = i;

            Thread inputThread = new Thread(() -> {
                try {
                    VertexHttpClient client = new VertexHttpClient();

                    List<IndexedMatrixBatch> inputBatch = new ArrayList<>();
                    inputBatch.add(new IndexedMatrixBatch(batchSequenceId, matLists.get(batchSequenceId)));

                    InfinityMessage msg = MatrixDoubleSequenceBatchBlockMessage.createInfinityMessage(
                            String.valueOf(1), "source1", "source2", "edge1", "QKTV", inputBatch);

                    InfinityMessageWrapper input1 = new InfinityMessageWrapper(msg);
                    MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();

                    // Unique key per head: K5-2.0.111-0 ... K5-2.0.111-13
                    mu.setKey("QKTV");
                    mu.setValue(input1);

                    String json = new Helper().getJson(mu);
                    System.out.println("[INPUT] head " + batchSequenceId + " key=" + mu.getKey());
                    System.out.println(json);

                    URI uri = URI.create("http://localhost:20401/input?vertexid=HS0");
                    System.out.println("[INPUT] POST -> " + uri);

                    client.sendDataSync(uri, json);

                } catch (Exception e) {
                    System.err.println("[INPUT] head " + batchSequenceId + " failed: " + e.getMessage());
                    throw new RuntimeException("Head " + batchSequenceId + " failed", e);
                }
            });

            threads.add(inputThread);
        }

        for (Thread thread : threads) {
            thread.start();
        }
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