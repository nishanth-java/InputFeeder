package mlpFeeders;

import java.util.BitSet;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class NayaLambdaBase {
    Map<String, Predicate<CustomMemory<?,?>>> predicateMap;
    Map<String, Integer> expectedInputs;
    BitSet receivedInputs;
    private int indexCounter;

    public void addPredicate(String catType, Predicate<CustomMemory<?,?>> predicate){
        /*boolean status = predicate.test(new CustomMemory<>());
        if(!status){
            throw new IllegalArgumentException("pagal ho kya");
        }*/
        int index = indexCounter++;
        predicateMap.put(catType,predicate);

        expectedInputs.merge(catType, 1, Integer::sum);
        receivedInputs.set(index);
    }
}
