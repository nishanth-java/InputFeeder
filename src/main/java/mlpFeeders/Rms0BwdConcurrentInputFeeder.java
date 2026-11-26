package mlpFeeders;


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
 * Reads Q_0..Q_4 from rms0bwdinputs.json and sends each as its own
 * MatrixSequenceBatchBlockMessage to vertex RMS0, all concurrently.
 */
public final class Rms0BwdConcurrentInputFeeder {

    private static final String DEFAULT_INPUT_PATH = "C://Users/willi/Downloads/rms0bwdinputs.json";

    private static final String HOST = "localhost";
    private static final int PORT = 20000;
    private static final String VERTEX_ID = "RMS0";
    private static final URI VERTEX_URI =
            URI.create("http://" + HOST + ":" + PORT + "/input?vertexid=" + VERTEX_ID);

    private static final String Q_KEY_FORMAT = "Q_%d";
    private static final String VALUES_FIELD = "values";

    private static final String MESSAGE_ID = "1";
    private static final String SOURCE_1 = "source1";
    private static final String SOURCE_2 = "source2";
    private static final String EDGE = "edge1";
    private static final String MEMORY_UNIT_KEY = "K5-2.0.111-";
    private static final int MATRIX_INDEX = 0;

    /** Q index -> category (message target), in send order. */
    private static final String[] CATEGORIES = {
            "delta_v",  // Q_0
            "delta_v",  // Q_1
            "delta_q",  // Q_2
            "delta_k",  // Q_3
            "delta_k"   // Q_4
    };

    private Rms0BwdConcurrentInputFeeder() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String inputPath = args.length > 0 ? args[0] : DEFAULT_INPUT_PATH;
        JsonNode root = new ObjectMapper().readTree(new File(inputPath));

        // Load every matrix first so missing/bad data fails before anything is sent.
        List<QInput> inputs = new ArrayList<>(CATEGORIES.length);
        for (int qNum = 0; qNum < CATEGORIES.length; qNum++) {
            inputs.add(new QInput(
                    String.format(Q_KEY_FORMAT, qNum),
                    CATEGORIES[qNum],
                    toMatrix(requireValues(root, qNum))));
        }

        sendAll(inputs);
    }

    /** Sends all inputs concurrently, waits for all to finish, and surfaces any failure. */
    private static void sendAll(List<QInput> inputs) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(inputs.size());
        try {
            List<Future<?>> futures = new ArrayList<>(inputs.size());
            for (QInput input : inputs) {
                futures.add(executor.submit(() -> {
                    send(input);
                    return null;
                }));
            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    throw new IllegalStateException("Failed to send Q input", e.getCause());
                }
            }
        } finally {
            executor.shutdown();
        }
    }

    /** Builds and sends the message for a single Q_n block using its category as the target. */
    private static void send(QInput input) throws Exception {
        List<IndexedMatrix> matrices =
                Collections.singletonList(new IndexedMatrix(MATRIX_INDEX, input.matrix));

        InfinityMessage message = MatrixSequenceBatchBlockMessage.createInfinityMessage(
                MESSAGE_ID, SOURCE_1, SOURCE_2, EDGE, input.category, matrices);

        MemoryUnit<String, InfinityMessageWrapper> memoryUnit = new MemoryUnit<>();
        memoryUnit.setKey(MEMORY_UNIT_KEY);
        memoryUnit.setValue(new InfinityMessageWrapper(message));

        String json = new Helper().getJson(memoryUnit);
        System.out.println(input.key + " (" + input.category + ") payload: " + json);
        System.out.println("[INPUT] POST -> " + VERTEX_URI);

        // One client per task, so no client state is shared across threads.
        new VertexHttpClient().sendDataSync(VERTEX_URI, json);
    }

    private static JsonNode requireValues(JsonNode root, int qNum) {
        String key = String.format(Q_KEY_FORMAT, qNum);
        JsonNode node = root.get(key);
        if (node == null || node.get(VALUES_FIELD) == null) {
            throw new IllegalArgumentException(
                    "Missing '" + key + "." + VALUES_FIELD + "' in input file");
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

    private static final class QInput {
        private final String key;
        private final String category;
        private final double[][] matrix;

        private QInput(String key, String category, double[][] matrix) {
            this.key = key;
            this.category = category;
            this.matrix = matrix;
        }
    }
}
