package mlpFeeders;//package org.minio.inputfeederthing;
//
//import com.ziroh.infinity.communication.message.base.TypedInputBase;
//import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;
//import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessage;
//import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.MatrixSequenceBatchBlockMessagePredicate;
//import com.ziroh.infinity.lambda.lambdaBase.LambdaBase;
//import com.ziroh.infinity.lambda.lambdas.batchBlockMessageLambdas.LambdaElementWiseMultiplyBatchBlockMessage;
//import com.ziroh.infinity.memory.exceptions.CollectionDoesNotExistException;
//import com.ziroh.infinity.memory.exceptions.DBExistException;
//import com.ziroh.infinity.memory.exceptions.KeyDoesNotExistException;
//import com.ziroh.infinity.memory.exceptions.ObjectConversionException;
//import com.ziroh.infinity.util.MathOps;
//import com.ziroh.infinity.vertex.InfinityVertex;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//public class LambdaMatAddArya extends LambdaBase {
//
//    public static Logger logger = LoggerFactory.getLogger(LambdaMatAddArya.class);
//    @Override
//    public Optional<TypedInputBase<?, ?>> computeBiFunction(InfinityVertex infinityVertex, Object[] objects) {
//
//        try{
//            String key1 =(String) objects[0];
//            String key2 =(String) objects[1];
//
//            MatrixSequenceBatchBlockMessage message1 =(MatrixSequenceBatchBlockMessage) infinityVertex.findRetrieve("DEFAULT DB","Working memory",key1);
//            MatrixSequenceBatchBlockMessage message2 =(MatrixSequenceBatchBlockMessage) infinityVertex.findRetrieve("DEFAULT DB","Working memory",key2);
//
//            MatrixSequenceBatchBlockMessage.assertMatrixSequenceBatchBlockMessage(message1,message2);
//          List<IndexedMatrix> resultList = new ArrayList<>();
//            for (int i = 0; i < message1.getValue().size(); i++) {
//                double[][] result = MathOps.add(message1.getValue().get(i).getMatrix(),message2.getValue().get(i).getMatrresultList.add(new IndexedMatrix(0,result));
//            }
//
//            logger.info("Output for lambda- {} on vertex {} :-> {}", new Object[]{this.getLambdaId(), infinityVertex.getId(), ((IndexedMatrix)resultList.getFirst()).getMatrix()});
//            return Optional.of(MatrixSequenceBatchBlockMessage.create("e", infinityVertex.getId(), message1.getDestinationId(), "e", this.getOutputCategoryType(), resultList));
//
//        } catch (KeyDoesNotExistException e) {
//            throw new RuntimeException(e);
//        } catch (ObjectConversionException e) {
//            throw new RuntimeException(e);
//        } catch (DBExistException e) {
//            throw new RuntimeException(e);
//        } catch (CollectionDoesNotExistException e) {
//            throw new RuntimeException(e);
//        }
//
//    }
//
//    public static LambdaMatAddArya create(String lambdaId, String inputDB, String inputCollection, String outputDB, String outputCollection, String outputCategoryType, String inputCategoryType1, String inputCategoryType2, boolean returnOutput) {
//        LambdaMatAddArya lambda= new LambdaMatAddArya();
//        lambda.setLambdaId(lambdaId);
//        lambda.setInputDB(inputDB);
//        lambda.setInputCollection(inputCollection);
//        lambda.setOutputDB(outputDB);
//        lambda.setOutputCollection(outputCollection);
//        lambda.setOutputCategoryType(outputCategoryType);
//        lambda.setReturnOutput(returnOutput);
//        lambda.addPredicate(inputCategoryType1, MatrixSequenceBatchBlockMessage.class, MatrixSequenceBatchBlockMessagePredicate.getPredicate());
//        lambda.addPredicate(inputCategoryType2, MatrixSequenceBatchBlockMessage.class, MatrixSequenceBatchBlockMessagePredicate.getPredicate());
//        return lambda;
//    }
//
//}
