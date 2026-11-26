package inputfeeder;

import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class EmbedFwdInputFeeder {

    public static void main(String[] args) {

        //----------INPUT THREAD-------//

        Thread inputThread = new Thread(() -> {
            try
            {
                VertexHttpClient client = new VertexHttpClient();

                double[][] b0Tokens =
                        {
                                {3040, 825, 1378, 2326},
                                {825, 1378, 2326, 3040},
                                {1378, 2326, 3040, 825}
                        };
               /* double[][] b1Tokens =
                        {
                                {3040, 825, 1378, 2326},
                                {825, 1378, 2326, 3040},
                                {1378, 2326, 3040, 825}
                        };
                double[][] b2Tokens =
                        {
                                {3040, 825, 1378, 2326},
                                {825, 1378, 2326, 3040},
                                {1378, 2326, 3040, 825}
                        };*/

                List<IndexedMatrix> matrixList = new ArrayList<>();
                matrixList.add( IndexedMatrix.exact(0,b0Tokens));
                
            /*    inputList.add(new IndexedMatrix(1,b1Tokens));
                inputList.add(new IndexedMatrix(2,b2Tokens));*/

                InfinityMessage msg = MatrixSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                        "source2","edge1", "TOKEN", matrixList);

                InfinityMessageWrapper input1 = new InfinityMessageWrapper(msg);
                MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
                mu.setKey("K5-" + "2.0.111-");
                mu.setValue(input1);

                String json = new Helper().getJson(mu);

                System.out.println(json);
                URI uri = URI.create(
                        "http://localhost:9032/input?vertexid=EMBED"
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
