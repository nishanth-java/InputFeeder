package prime;

import com.ziroh.infinity.communication.message.base.TypedInputBase;
import com.ziroh.infinity.lambda.input.inputMatrixMessage.InputMatrixMessage;
import com.ziroh.infinity.lambda.lambdaBase.LambdaBase;
import com.ziroh.infinity.lambda.lambdas.lambdaInputMatrixMessage.LambdaMatMulWtWithBias;
import com.ziroh.infinity.memory.exceptions.CollectionDoesNotExistException;
import com.ziroh.infinity.memory.exceptions.DBExistException;
import com.ziroh.infinity.memory.exceptions.KeyDoesNotExistException;
import com.ziroh.infinity.memory.exceptions.ObjectConversionException;
import com.ziroh.infinity.vertex.InfinityVertex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LambdaPrime extends LambdaBase {
    @Override
    public Optional<TypedInputBase<?, ?>> computeBiFunction(InfinityVertex infinityVertex, Object[] inputs) {

        try {
            InputMatrixMessage inputMatrixMessage = (InputMatrixMessage)
                    infinityVertex.findRetrieve(this.getInputDB(),this.getInputCollection(),inputs[0]);

            double low = inputMatrixMessage.getValue()[0][0];
            double high = inputMatrixMessage.getValue()[0][1];

            List<Integer> primes = getPrimesInRange((int)low,(int) high);
            double[][] result = new double[1][primes.size()];
            for(int i=0;i<primes.size();i++){
                result[0][i] = primes.get(i);
            }


            return Optional.of(InputMatrixMessage.create(
                    inputMatrixMessage.getMessageId().split("::")[0],
                    infinityVertex.getId(),
                    inputMatrixMessage.getDestinationId(),
                    "e",
                    this.getOutputCategoryType(),
                    result
            ));



        } catch (DBExistException | CollectionDoesNotExistException | KeyDoesNotExistException |
                 ObjectConversionException e) {
            throw new RuntimeException(e);
        }


    }

    public static LambdaPrime create(int noOfLayers, String lambdaId, String inputDB, String inputCollection, String outputDB, String outputCollection, String outputCategoryType, boolean returnOutput) {
        LambdaPrime lambda = new LambdaPrime();
        lambda.setLambdaId(lambdaId);
        lambda.setInputDB(inputDB);
        lambda.setInputCollection(inputCollection);
        lambda.setOutputDB(outputDB);
        lambda.setOutputCollection(outputCollection);
        lambda.setOutputCategoryType(outputCategoryType);
        lambda.setReturnOutput(returnOutput);
        return lambda;
    }


    // Helper method to find all base primes up to sqrt(R) using standard Sieve
    private static List<Integer> getBasePrimes(int limit) {
        boolean[] isPrime = new boolean[limit + 1];
        Arrays.fill(isPrime, true);
        List<Integer> primes = new ArrayList<>();

        for (int p = 2; p * p <= limit; p++) {
            if (isPrime[p]) {
                for (int i = p * p; i <= limit; i += p) {
                    isPrime[i] = false;
                }
            }
        }

        for (int p = 2; p <= limit; p++) {
            if (isPrime[p]) {
                primes.add(p);
            }
        }
        return primes;
    }

    // Main method to find primes in the range [low, high] efficiently
    public static List<Integer> getPrimesInRange(int low, int high) {
        List<Integer> result = new ArrayList<>();
        if (low > high || high < 2) {
            return result;
        }
        if (low < 2) {
            low = 2; // 0 and 1 are not prime numbers
        }

        // Find all base primes up to sqrt(high)
        int limit = (int) Math.sqrt(high);
        List<Integer> basePrimes = getBasePrimes(limit);

        // Boolean array representing the window [low, high]
        int rangeSize = high - low + 1;
        boolean[] isPrimeInRange = new boolean[rangeSize];
        Arrays.fill(isPrimeInRange, true);

        // Mark multiples of base primes in the current range
        for (int prime : basePrimes) {
            // Find the minimum number in [low, high] that is a multiple of 'prime'
            int startIdx = (low / prime) * prime;
            if (startIdx < low) {
                startIdx += prime;
            }

            // If the startIdx is the prime itself, move to the next multiple
            if (startIdx == prime) {
                startIdx += prime;
            }

            // Mark all multiples as false
            for (int j = startIdx; j <= high; j += prime) {
                isPrimeInRange[j - low] = false;
            }
        }

        // Collect all indices that remain true
        for (int i = 0; i < rangeSize; i++) {
            if (isPrimeInRange[i]) {
                result.add(i + low);
            }
        }

        return result;
    }
}
