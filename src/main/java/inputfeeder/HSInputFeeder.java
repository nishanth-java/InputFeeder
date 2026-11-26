package inputfeeder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziroh.infinity.communication.client.http.VertexHttpClient;
import com.ziroh.infinity.communication.message.base.InfinityMessage;
import com.ziroh.infinity.lambda.input.inputMatrixMessage.InputMatrixMessage;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.IndexedMatrixBatch;
import com.ziroh.infinity.lambda.input.matrixDoubleSequenceBatchBlockMessage.MatrixDoubleSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.lambda.lambdaInputQ.adapter.InfinityMessageWrapper;
import com.ziroh.infinity.memory.common.Helper;
import com.ziroh.infinity.memory.common.MemoryUnit;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class HSInputFeeder {

    public static void main(String[] args) throws IOException {

        //----------INPUT THREAD-------//
        List<Thread> threads = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File("C:/Users/nisha/Downloads/forward_builtin_new_checkpoints.json"));

        List<IndexedMatrix>mat0List = new ArrayList<>();
        List<IndexedMatrix>mat1List = new ArrayList<>();
        List<IndexedMatrix>mat2List = new ArrayList<>();
        List<IndexedMatrix>mat3List = new ArrayList<>();
        List<IndexedMatrix>mat4List = new ArrayList<>();
        List<IndexedMatrix>mat5List = new ArrayList<>();
        List<IndexedMatrix>mat6List = new ArrayList<>();
        List<IndexedMatrix>mat7List = new ArrayList<>();
        List<IndexedMatrix>mat8List = new ArrayList<>();
        List<IndexedMatrix>mat9List = new ArrayList<>();
        List<IndexedMatrix>mat10List = new ArrayList<>();
        List<IndexedMatrix>mat11List = new ArrayList<>();
        List<IndexedMatrix>mat12List = new ArrayList<>();
        List<IndexedMatrix>mat13List = new ArrayList<>();

        mat0List .add(new IndexedMatrix(0, toMatrix(root.get("b0.layer0.head0.attn_out").get("values"))));
        mat1List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head1.attn_out").get("values"))));
        mat2List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head2.attn_out").get("values"))));
        mat3List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head3.attn_out").get("values"))));
        mat4List .add(new IndexedMatrix(0, toMatrix(root.get("b0.layer0.head4.attn_out").get("values"))));
        mat5List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head5.attn_out").get("values"))));
        mat6List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head6.attn_out").get("values"))));
        mat7List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head7.attn_out").get("values"))));
        mat8List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head8.attn_out").get("values"))));
        mat9List .add(new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head9.attn_out").get("values"))));
        mat10List .add(new IndexedMatrix(0, toMatrix(root.get("b0.layer0.head10.attn_out").get("values"))));
        mat11List .add( new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head11.attn_out").get("values"))));
        mat12List .add( new IndexedMatrix(0,toMatrix(root.get("b0.layer0.head12.attn_out").get("values"))));
        mat13List .add(new IndexedMatrix(0, toMatrix(root.get("b0.layer0.head13.attn_out").get("values"))));



        for(int i=0;i<14;i++) {
            final int batchSequenceId = i;
            Thread inputThread = new Thread(() -> {
                try {
                    VertexHttpClient client = new VertexHttpClient();

          /*  double [][] mat = new double [5][896];
            for(int i=0;i<5;i++)
            {
                for(int j=0;j<896;j++)
                {
                    mat[i][j]=i+j;
                }
            }
          */
                    List<IndexedMatrixBatch> inputBatch= new ArrayList<>();

                /*    List<IndexedMatrixBatch> batch = new ArrayList<>();
                    batch.add(new IndexedMatrixBatch(batchSequenceId, inputList));*/
                    switch (batchSequenceId) {
                        case 0 : inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat0List));
                                    break;
                        case 1: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat1List));
                                    break;
                        case 2: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat2List));
                            break;
                        case 3: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat3List));
                            break;
                        case 4: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat4List));
                            break;
                        case 5: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat5List));
                            break;
                        case 6: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat6List));
                            break;
                        case 7: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat7List));
                            break;
                        case 8: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat8List));
                            break;
                        case 9: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat9List));
                            break;
                        case 10: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat10List));
                            break;
                        case 11: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat11List));
                            break;
                        case 12: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat12List));
                            break;
                        case 13: inputBatch.add(new IndexedMatrixBatch(batchSequenceId, mat13List));
                            break;
                        default :
                            System.out.println("Invalid");
                    }

                    InfinityMessage msg = MatrixDoubleSequenceBatchBlockMessage.createInfinityMessage(String.valueOf(1), "source1",
                            "source2", "edge1", "QKTV", inputBatch);

                    InfinityMessageWrapper input1 = new InfinityMessageWrapper(msg);
                    MemoryUnit<String, InfinityMessageWrapper> mu = new MemoryUnit<>();
                    mu.setKey("K5-" + "2.0.111-");
                    mu.setValue(input1);

                    String json = new Helper().getJson(mu);

                    System.out.println(json);
                    URI uri = URI.create(
                            "http://localhost:20401/input?vertexid=HS0"
                    );

                    System.out.println("[INPUT] POST -> " + uri);

                    client.sendDataSync(uri, json);

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            threads.add(inputThread);

        }
        for(Thread thread : threads)
        {
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