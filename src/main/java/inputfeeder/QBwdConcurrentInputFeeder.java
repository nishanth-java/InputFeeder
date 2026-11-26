package inputfeeder;

import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;
import util.UtilHelper;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Reads delta0.json and sends it as a single MatrixSequenceBatchBlockMessage
 * (target "delta_o") to vertex Q_0.
 */
public final class QBwdConcurrentInputFeeder {

    private static final String DEFAULT_INPUT_PATH =
            "D:/Infinity/RemoteServer/OutputVertices/delta0.json";

    private static final String HOST = "localhost";
    private static final int PORT = 20003;
    private static final String VERTEX_ID = "Q_0";
    private static final URI VERTEX_URI =
            URI.create("http://" + HOST + ":" + PORT + "/input?vertexid=" + VERTEX_ID);

    private static final String MESSAGE_ID = "1";
    private static final String SOURCE_1 = "source1";
    private static final String SOURCE_2 = "source2";
    private static final String EDGE = "edge1";
    private static final String TARGET = "delta_o";
    private static final String MEMORY_UNIT_KEY = "K5-2.0.111-";
    private static final int MATRIX_INDEX = 0;

    private QBwdConcurrentInputFeeder() {
    }

    public static void main(String[] args) throws Exception {
        String inputPath = args.length > 0 ? args[0] : DEFAULT_INPUT_PATH;
        double[][] matrix = UtilHelper.readJsonMatrix(new File(inputPath), null);

        send(matrix);
    }

    private static void send(double[][] matrix) throws Exception {
        List<IndexedMatrix> matrices =
                Collections.singletonList(new IndexedMatrix(MATRIX_INDEX, matrix));

        InfinityMessage message = MatrixSequenceBatchBlockMessage.createInfinityMessage(
                MESSAGE_ID, SOURCE_1, SOURCE_2, EDGE, TARGET, matrices);

        MemoryUnit<String, InfinityMessageWrapper> memoryUnit = new MemoryUnit<>();
        memoryUnit.setKey(MEMORY_UNIT_KEY);
        memoryUnit.setValue(new InfinityMessageWrapper(message));

        String json = new Helper().getJson(memoryUnit);
        System.out.println(json);
        System.out.println("[INPUT] POST -> " + VERTEX_URI);

        new VertexHttpClient().sendDataSync(VERTEX_URI, json);
    }
}