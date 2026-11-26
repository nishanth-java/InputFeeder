package inputfeeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;
import com.ziroh.infinity.util.MathOps;
import util.UtilHelper;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class KFwdInputFeeder {

    public static void main(String[] args) {

        //----------INPUT THREAD-------//

        Thread inputThread = new Thread(() -> {
            try
            {
                VertexHttpClient client = new VertexHttpClient();

                File rms0 = new File("D:/Infinity/RemoteServer/OutputVertices/RMS0FwdOutput.json");
                double[][] rms0matrix = UtilHelper.readJsonMatrix(rms0, null);

                List<IndexedMatrix> rms0InputList = new ArrayList<>();
                rms0InputList.add(new IndexedMatrix(0, rms0matrix));


                InfinityMessage rms0msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                                                                                                "source2","edge1", "rms", rms0InputList);
                InfinityMessageWrapper rms0input = new InfinityMessageWrapper(rms0msg);
                MemoryUnit<String, InfinityMessageWrapper> rms0mu = new MemoryUnit<>();
                rms0mu.setKey("K5-" + "2.0.111-");
                rms0mu.setValue(rms0input);

                String rms0json = new Helper().getJson(rms0mu);
                System.out.println(rms0json);
                URI rms0uri = URI.create(
                        "http://localhost:20001/input?vertexid=K0"
                );

                System.out.println("[INPUT] POST -> " + rms0uri);

                client.sendDataSync(rms0uri, rms0json);

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
