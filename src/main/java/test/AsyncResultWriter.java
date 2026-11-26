package test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AsyncResultWriter {

    private static final ExecutorService EXECUTOR =
            Executors.newFixedThreadPool(2);

    private AsyncResultWriter() {}

    public static CompletableFuture<Void> writeAsync(
            String outputValueName,
            List<IndexedMatrix> matrices) {

        return CompletableFuture.runAsync(() -> {

            try {

                writeOutput(
                        outputValueName,
                        matrices
                );

            } catch (IOException e) {

                throw new RuntimeException(
                        "Failed to write output: "
                                + outputValueName,
                        e
                );

            }

        }, EXECUTOR);
    }



    private static void writeOutput(
            String outputValueName,
            List<IndexedMatrix> matrices)
            throws IOException {

        File resultsDirectory = new File("results");

        if (!resultsDirectory.exists() && !resultsDirectory.mkdirs()) {
            throw new IOException(
                    "Failed to create results directory: "
                            + resultsDirectory.getAbsolutePath()
            );
        }

        File outputFile = new File(
                resultsDirectory,
                outputValueName + ".json"
        );

        JsonFactory factory = new JsonFactory();

        try (
                FileOutputStream outputStream =
                        new FileOutputStream(outputFile);

                JsonGenerator generator =
                        factory.createGenerator(outputStream)
        ) {

            generator.useDefaultPrettyPrinter();

            generator.writeStartObject();

            for (IndexedMatrix indexedMatrix : matrices) {

                int sequenceId =
                        indexedMatrix.getSequenceId();

                double[][] matrix =
                        indexedMatrix.getMatrix();


                generator.writeObjectFieldStart(
                        outputValueName + ".s" + sequenceId
                );


                generator.writeArrayFieldStart("values");


                for (double[] row : matrix) {

                    generator.writeStartArray();

                    for (double value : row) {
                        generator.writeNumber(value);
                    }

                    generator.writeEndArray();
                }


                generator.writeEndArray();


                generator.writeEndObject();
            }


            generator.writeEndObject();
        }
    }

    public static void shutdown() {

        EXECUTOR.shutdown();
    }
}