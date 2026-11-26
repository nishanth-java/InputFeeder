package prime;

import com.ziroh.infinity.examples.CollectionsConstant;
import com.ziroh.infinity.lambda.input.inputMatrixMessage.InputMatrixMessage;
import com.ziroh.infinity.lambda.input.inputMatrixMessage.InputMatrixMessagePredicate;
import com.ziroh.infinity.lambda.lambdaBase.LambdaBase;
import com.ziroh.infinity.lambda.lambdaBase.LambdaContainer;
import com.ziroh.infinity.lambda.lambdaFactory.LambdaFactory;
import com.ziroh.infinity.lambda.lambdaInputQ.InputQData;
import com.ziroh.infinity.lambda.lambdas.LambdaType;
import com.ziroh.infinity.memory.collection.types.ConcurrentHashMapCollection;
import com.ziroh.infinity.memory.exceptions.DBExistException;
import com.ziroh.infinity.util.CollectionFactory;
import com.ziroh.infinity.vertex.InfinityVertex;
import com.ziroh.infinity.vertex.VertexBuilder;

public class VertexPrime {

    public static InfinityVertex createVertex(
            String vertexId,
            String vertexName,
            String hostName,
            int httpPort,
            int wsPort, int noOfWeightRows, int noOfLayers) {
        try {
            InfinityVertex vertex =
                    VertexBuilder.newVertexBuilder()
                            .setID(vertexId)
                            .setVertexName(vertexName)
                            .setPortNumber(httpPort)
                            .setWSPort(wsPort)
                            .setHostName(hostName)
                            .configure().
                            build();

            vertex.createDB(CollectionsConstant.DB_NAME);

            ConcurrentHashMapCollection<String, InputMatrixMessage> workingMemory =
                    CollectionFactory.createConcurrentHashMapCollection(vertex, CollectionsConstant.DB_NAME,
                            CollectionsConstant.WORKING_MEMORY, String.class, InputMatrixMessage.class);


            LambdaPrime lambdaPrime = LambdaPrime.create(noOfLayers, "lambda-matmulWt",
                    CollectionsConstant.DB_NAME, CollectionsConstant.WORKING_MEMORY,
                    CollectionsConstant.DB_NAME, CollectionsConstant.OUTPUT_MEMORY,
                    "Q",false);

            //creating lambda container
            LambdaContainer lambdaWorkingMemContainer = new LambdaContainer();
            lambdaWorkingMemContainer.registerLambda(lambdaPrime);

            //Adding lambdaContainer to working memory
            workingMemory.getCollectionEvents().onNewDataAdd("LWMC", lambdaWorkingMemContainer::onIncomingData);
            workingMemory.getCollectionEvents().onDataUpdate("LWMC", lambdaWorkingMemContainer::onIncomingData);

            //Creating instance of lambdaPropagate, invoke onVertexCompletion
            LambdaBase lambdaPropagate = LambdaFactory.getLambda(LambdaType.PROPAGATE,"LPROP",
                    CollectionsConstant.DB_NAME, CollectionsConstant.OUTPUT_MEMORY,
                    null, null, "RMS", false );


            lambdaPropagate.addPredicate(lambdaPrime.getOutputCategoryType(), InputMatrixMessage.class, InputMatrixMessagePredicate.getPredicate());

            LambdaContainer lambdaOutputMemContainer = new LambdaContainer();
            lambdaOutputMemContainer.registerLambda(lambdaPropagate);

            //---OUTPUT MEMORY
            ConcurrentHashMapCollection<String, InputMatrixMessage> outputMemory =
                    CollectionFactory.createConcurrentHashMapCollection(vertex, CollectionsConstant.DB_NAME,
                            CollectionsConstant.OUTPUT_MEMORY,String.class, InputMatrixMessage.class);

            //---Adding lambda to output memory
            outputMemory.getCollectionEvents().onNewDataAdd(lambdaPropagate.getLambdaId(), lambdaOutputMemContainer::onIncomingData);
            outputMemory.getCollectionEvents().onDataUpdate(lambdaPropagate.getLambdaId(),lambdaOutputMemContainer::onIncomingData);

            vertex.onInputDataReceive(InputQData::onInputQDataAdd);

            return vertex;

        } catch (DBExistException e) {
            throw new RuntimeException(e);
        }
    }
}