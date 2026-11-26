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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class InputFeederHSBWD {

    private static final int TOTAL_INPUTS = 14;

    public static void main(String[] args) {

        Thread inputThread = new Thread(() -> {

            ExecutorService executor =
                    Executors.newFixedThreadPool(TOTAL_INPUTS);

            try {

                // ---------------------------------------------------------
                // Read input JSON
                // ---------------------------------------------------------

                ObjectMapper mapper = new ObjectMapper();

                JsonNode root = mapper.readTree(
                        new File(
                                "C://Users/willi/Downloads/input_grads_Sept4 (1).json"
                        )
                );

                // ---------------------------------------------------------
                // Get delta_hsbwd_custom_ip
                // ---------------------------------------------------------

                JsonNode values =
                        root.get("delta_hsbwd_custom_ip");

                // ---------------------------------------------------------
                // Convert JSON matrix -> double[][]
                // ---------------------------------------------------------

                double[][] mat = toMatrix(values);

                System.out.println(
                        "Matrix from file is => "
                                + mat.length
                                + " x "
                                + mat[0].length
                );

                // ---------------------------------------------------------
                // Target URI
                // ---------------------------------------------------------

                URI uri = URI.create(
                        "http://localhost:20005/input?vertexid=HS_BWD"
                );

                System.out.println(
                        "[INPUT] Target -> " + uri
                );

                // ---------------------------------------------------------
                // Submit all 14 inputs concurrently
                // ---------------------------------------------------------

                List<Future<?>> futures = new ArrayList<>();

                for (int i = 0; i < TOTAL_INPUTS; i++) {

                    final int index = i;

                    Future<?> future = executor.submit(() -> {

                        try {

                            System.out.println(
                                    "[INPUT-" + index + "] Starting"
                            );

                            // -------------------------------------------------
                            // Create client for this task
                            // -------------------------------------------------

                            VertexHttpClient client =
                                    new VertexHttpClient();

                            // -------------------------------------------------
                            // Create ONE IndexedMatrixBatch
                            // -------------------------------------------------

                            List<IndexedMatrixBatch> inputList =
                                    new ArrayList<>();

                            inputList.add(
                                    new IndexedMatrixBatch(
                                            index,
                                            List.of(
                                                    new IndexedMatrix(0, mat)
                                            )
                                    )
                            );

                            // -------------------------------------------------
                            // Create ONE MatrixDoubleSequenceBatchBlockMessage
                            // -------------------------------------------------

                            InfinityMessage msg =
                                    MatrixDoubleSequenceBatchBlockMessage
                                            .createInfinityMessage(
                                                    String.valueOf(1),
                                                    "source1",
                                                    "source2",
                                                    "edge1",
                                                    "delta_q",
                                                    inputList
                                            );

                            // -------------------------------------------------
                            // Wrap message
                            // -------------------------------------------------

                            InfinityMessageWrapper input =
                                    new InfinityMessageWrapper(msg);

                            // -------------------------------------------------
                            // Create MemoryUnit
                            // -------------------------------------------------

                            MemoryUnit<String, InfinityMessageWrapper> mu =
                                    new MemoryUnit<>();

                            mu.setKey(
                                    "K5-" + "2.0.111-" + index
                            );

                            mu.setValue(input);

                            // -------------------------------------------------
                            // Convert message to JSON
                            // -------------------------------------------------

                            String json =
                                    new Helper().getJson(mu);

                            // -------------------------------------------------
                            // Send message
                            // -------------------------------------------------

                            System.out.println(
                                    "[INPUT-" + index + "] Sending..."
                            );

                            client.sendDataSync(uri, json);

                            System.out.println(
                                    "[INPUT-" + index + "] SENT successfully"
                            );

                        } catch (Exception e) {

                            System.err.println(
                                    "[INPUT-" + index + "] FAILED"
                            );

                            e.printStackTrace();

                            throw new RuntimeException(e);
                        }
                    });

                    futures.add(future);
                }

                // ---------------------------------------------------------
                // Wait for all 14 tasks to finish
                // ---------------------------------------------------------

                for (Future<?> future : futures) {
                    future.get();
                }

                System.out.println(
                        "\n========== ALL 14 INPUTS COMPLETED =========="
                );

            } catch (Exception e) {

                throw new RuntimeException(e);

            } finally {

                // ---------------------------------------------------------
                // Shutdown executor
                // ---------------------------------------------------------

                executor.shutdown();

                System.out.println(
                        "[INPUT] Executor shutdown"
                );
            }

        });

        inputThread.start();
    }

    // ---------------------------------------------------------------------
    // Convert JsonNode -> double[][]
    // ---------------------------------------------------------------------

    private static double[][] toMatrix(JsonNode valuesNode) {

        double[][] mat =
                new double[valuesNode.size()][];

        for (int i = 0; i < valuesNode.size(); i++) {

            JsonNode row =
                    valuesNode.get(i);

            mat[i] =
                    new double[row.size()];

            for (int j = 0; j < row.size(); j++) {

                mat[i][j] =
                        row.get(j).asDouble();
            }
        }

        return mat;
    }
}
