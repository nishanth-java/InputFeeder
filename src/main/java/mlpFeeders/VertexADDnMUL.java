package mlpFeeders;

import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
import com.ziroh.infinity.memory.collection.types.ConcurrentHashMapCollection;
import com.ziroh.infinity.memory.exceptions.*;
import com.ziroh.infinity.util.CollectionFactory;
import com.ziroh.infinity.util.CsvFileFinder;
import com.ziroh.infinity.util.CsvReader;
import com.ziroh.infinity.util.MathOps;
import com.ziroh.infinity.vertex.InfinityVertex;
import com.ziroh.infinity.vertex.VertexBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VertexADDnMUL {

    public InfinityVertex createVertex(String vertexId, String vertexName, String hostName, int httpPort, int wsPort, int noOfWeightRows, int noOfLayers, double learningRate) {

        try {
            InfinityVertex vertex = VertexBuilder.newVertexBuilder().setID(vertexId).setVertexName(vertexName).setPortNumber(httpPort).setWSPort(wsPort).setHostName(hostName).configure().build();
            vertex.createDB("DEFAULT DB");

            ConcurrentHashMapCollection<String, MatrixSequenceBatchBlockMessage> workingMemory = CollectionFactory.createConcurrentHashMapCollection(vertex,"DEFAULT DB","Working memory",String.class,MatrixSequenceBatchBlockMessage.class);

            for(int i = 0 ; i< noOfLayers ; i++){
                List<IndexedMatrix> weightList = new ArrayList<>();
                File weightFile = CsvFileFinder.findFileIgnoreCase("weight_" + i + "_" + vertexId + ".csv");
                double[][] weightMatrix= CsvReader.readMatrix(weightFile,0,64);
                weightMatrix = MathOps.transpose(weightMatrix);
                MatrixSequenceBatchBlockMessage weightMessage = MatrixSequenceBatchBlockMessage.create("e",vertexId,"d","e",vertexId,weightList);
                workingMemory.add(String.format("WeightKey_%s", i), weightMessage);

            }



            return vertex;
        } catch (DBExistException e) {
            throw new RuntimeException(e);
        }   catch (ObjectConversionException e) {
            throw new RuntimeException(e);
        } catch (KeyAlreadyExistException e) {
            throw new RuntimeException(e);
        }


    }
}