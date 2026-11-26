package inputfeeder;

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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Reads per-head attention outputs from a checkpoint JSON file and sends 14
 * MatrixDoubleSequenceBatchBlockMessages to the HS0 vertex, one per head.
 *
 * Each message (head i, i = 0..13):
 *   message_i
 *     +- IndexedMatrixBatch(i) -> [ IndexedMatrix(0, head_i matrix) ]
 */
public final class Hs_BwdInputFeeder {

    private static final String DEFAULT_CHECKPOINT_PATH =
            "C:/Users/nisha/Downloads/forward_builtin_new_checkpoints.json";

    private static final int NUM_HEADS = 14;
    private static final String HEAD_KEY_FORMAT = "b0.layer0.head%d.attn_out";
    private static final String VALUES_FIELD = "values";

    private static final String VERTEX_URI = "http://localhost:20005/input?vertexid=HS0";

    private static final String MESSAGE_ID = "1";
    private static final String SOURCE_1 = "source1";
    private static final String SOURCE_2 = "source2";
    private static final String EDGE = "edge1";
    private static final String TARGET = "delta_q";
    private static final String MEMORY_UNIT_KEY = "K5-2.0.111-";
    private static final int MATRIX_INDEX = 0;

    private Hs_BwdInputFeeder() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String checkpointPath = args.length > 0 ? args[0] : DEFAULT_CHECKPOINT_PATH;
        JsonNode root = new ObjectMapper().readTree(new File(checkpointPath));

        // Build all batches first so missing/bad data fails before anything is sent.
        List<IndexedMatrixBatch> batches = new ArrayList<>(NUM_HEADS);
        for (int head = 0; head < NUM_HEADS; head++) {
            double[][] matrix = toMatrix(requireValues(root, head));
            List<IndexedMatrix> matrices =
                    Collections.singletonList(new IndexedMatrix(MATRIX_INDEX, matrix));
            batches.add(new IndexedMatrixBatch(head, matrices));
        }

        sendAll(batches);
    }

    /** Sends one message per batch concurrently, waits for all, and surfaces any failure. */
    private static void sendAll(List<IndexedMatrixBatch> batches) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(batches.size());
        try {
            List<Future<?>> futures = new ArrayList<>(batches.size());
            for (IndexedMatrixBatch batch : batches) {
                futures.add(executor.submit(() -> {
                    send(batch);
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Failed to send message", e.getCause());
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    /** Sends one MatrixDoubleSequenceBatchBlockMessage containing exactly one batch. */
    private static void send(IndexedMatrixBatch batch) throws Exception {
        InfinityMessage message = MatrixDoubleSequenceBatchBlockMessage.createInfinityMessage(
                MESSAGE_ID, SOURCE_1, SOURCE_2, EDGE, TARGET, Collections.singletonList(batch));

        MemoryUnit<String, InfinityMessageWrapper> memoryUnit = new MemoryUnit<>();
        memoryUnit.setKey(MEMORY_UNIT_KEY);
        memoryUnit.setValue(new InfinityMessageWrapper(message));

        String json = new Helper().getJson(memoryUnit);
        URI uri = URI.create(VERTEX_URI);

        System.out.println(json);
        System.out.println("[INPUT] POST -> " + uri);

        new VertexHttpClient().sendDataSync(uri, json);
    }

    private static JsonNode requireValues(JsonNode root, int head) {
        String key = String.format(HEAD_KEY_FORMAT, head);
        JsonNode node = root.get(key);
        if (node == null || node.get(VALUES_FIELD) == null) {
            throw new IllegalArgumentException(
                    "Missing '" + key + "." + VALUES_FIELD + "' in checkpoint file");
        }
        return node.get(VALUES_FIELD);
    }

    private static double[][] toMatrix(JsonNode valuesNode) {
        double[][] matrix = new double[valuesNode.size()][];
        for (int i = 0; i < matrix.length; i++) {
            JsonNode row = valuesNode.get(i);
            matrix[i] = new double[row.size()];
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = row.get(j).asDouble();
            }
        }
        return matrix;
    }
}