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


public class QFwdInputFeeder {

    public static void main(String[] args) {

        //----------INPUT THREAD-------//

        Thread inputThread = new Thread(() -> {
            try
            {
                VertexHttpClient client = new VertexHttpClient();

                File k0 = new File("D:/Infinity/RemoteServer/OutputVertices/K0FwdOutput.json");
                double[][] k0matrix = UtilHelper.readJsonMatrix(k0, null);

                File v0 = new File("D:/Infinity/RemoteServer/OutputVertices/V0FwdOutput.json");
                double[][] v0matrix = UtilHelper.readJsonMatrix(v0, null);

                File rms0 = new File("D:/Infinity/RemoteServer/OutputVertices/RMS0FwdOutput.json");
                double[][] rms0matrix = UtilHelper.readJsonMatrix(rms0, null);

                List<IndexedMatrix> k0InputList = new ArrayList<>();
                k0InputList.add(new IndexedMatrix(0, k0matrix));

                List<IndexedMatrix> v0InputList = new ArrayList<>();
                v0InputList.add(new IndexedMatrix(0, v0matrix));

                List<IndexedMatrix> rms0InputList = new ArrayList<>();
                rms0InputList.add(new IndexedMatrix(0, rms0matrix));

                InfinityMessage k0msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                                                                                            "source2","edge1", "kt", k0InputList);
                InfinityMessage v0msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                                                                                              "source2","edge1", "v", v0InputList);
                InfinityMessage rms0msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                                                                                              "source2","edge1", "rms", rms0InputList);
                InfinityMessageWrapper k0input = new InfinityMessageWrapper(k0msg);
                MemoryUnit<String, InfinityMessageWrapper> k0mu = new MemoryUnit<>();
                k0mu.setKey("K5-" + "2.0.111-");
                k0mu.setValue(k0input);

                String k0json = new Helper().getJson(k0mu);
                System.out.println(k0json);
                URI k0uri = URI.create(
                        "http://localhost:20003/input?vertexid=Q_0"
                );

                System.out.println("[INPUT] POST -> " + k0uri);

                client.sendDataSync(k0uri, k0json);

                InfinityMessageWrapper v0input = new InfinityMessageWrapper(v0msg);
                MemoryUnit<String, InfinityMessageWrapper> v0mu = new MemoryUnit<>();
                v0mu.setKey("K5-" + "2.0.111-");
                v0mu.setValue(v0input);
                String v0json = new Helper().getJson(v0mu);
                System.out.println(v0json);
                URI v0uri = URI.create(
                        "http://localhost:20003/input?vertexid=Q_0"
                );

                System.out.println("[INPUT] POST -> " +v0uri);

                client.sendDataSync(v0uri, v0json);

                InfinityMessageWrapper rms0input = new InfinityMessageWrapper(rms0msg);
                MemoryUnit<String, InfinityMessageWrapper> rms0mu = new MemoryUnit<>();
                rms0mu.setKey("K5-" + "2.0.111-");
                rms0mu.setValue(rms0input);
                String rms0json = new Helper().getJson(rms0mu);
                System.out.println(rms0json);
                URI rms0uri = URI.create(
                        "http://localhost:20003/input?vertexid=Q_0"
                );

                System.out.println("[INPUT] POST -> " +rms0uri);

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
