package inputfeeder;

import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.IndexedMatrixBatch;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.MatrixDoubleSequenceBatchBlockMessage;
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


public class QBwdInputFeeder {

    public static void main(String[] args) {

        //----------INPUT THREAD-------//

        Thread inputThread = new Thread(() -> {
            try
            {
                VertexHttpClient client = new VertexHttpClient();
                File k0 = new File("D:/Infinity/RemoteServer/OutputVertices/delta0.json");
                double[][] deltaOmat = UtilHelper.readJsonMatrix(k0, null);
              //  double[][] deltaOmat = MathOps.transpose(deltaO);

                List<IndexedMatrix> matrixList = new ArrayList<>();
                matrixList.add(new IndexedMatrix(0,deltaOmat));

                InfinityMessage msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                                                                                            "source2","edge1", "delta_o", matrixList);

                InfinityMessageWrapper input1 = new InfinityMessageWrapper(msg);
                MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
                mu.setKey("K5-" + "2.0.111-");
                mu.setValue(input1);

                String json = new Helper().getJson(mu);

                System.out.println(json);
                URI uri = URI.create(
                        "http://localhost:20003/input?vertexid=Q_0"
                );

                System.out.println("[INPUT] POST -> " + uri);

                client.sendDataSync(uri, json);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        inputThread.start();
    }
}
