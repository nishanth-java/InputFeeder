/*
package test;

import com.ziroh.infinity.lambda.input.matrixSequenceBatchBlockMessage.IndexedMatrix;

import java.util.ArrayList;
import java.util.List;

import static org.reflections.Reflections.log;

public class LossTest {
    public static void main(String[] args) {

        List<IndexedMatrix> resultList = new ArrayList<>();
        double batchLoss = 0;

        for (IndexedMatrix indexedMatrix : inputMatrixMessage.getValue()) {
            double[] y_GT_colIdx = y_GT_Mat[indexedMatrix.getSequenceId()];
            double[][] sequenceMatrix = indexedMatrix.getMatrix();
            double sequenceLoss = 0;

            for (int row = 0; row < sequenceMatrix.length; row++) {
                sequenceMatrix[row][(int) y_GT_colIdx[row]]--;
                sequenceLoss += Math.log(sequenceMatrix[row][(int) y_GT_colIdx[row]]);
            }
            batchLoss += sequenceLoss;
            resultList.add(new IndexedMatrix(indexedMatrix.getSequenceId(), sequenceMatrix));
        }

        batchLoss = batchLoss / inputMatrixMessage.getValue().size();
        log.info("Loss for batch {} :-> {}", batchIdx, batchLoss);
    }
}
*/
