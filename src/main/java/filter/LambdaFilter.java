package filter;

import com.ziroh.infinity.communication.message.base.TypedInputBase;
import com.ziroh.infinity.lambda.input.inputMatrixMessage.InputMatrixMessage;
import com.ziroh.infinity.lambda.lambdaBase.LambdaBase;
import com.ziroh.infinity.memory.exceptions.CollectionDoesNotExistException;
import com.ziroh.infinity.memory.exceptions.DBExistException;
import com.ziroh.infinity.memory.exceptions.KeyDoesNotExistException;
import com.ziroh.infinity.memory.exceptions.ObjectConversionException;
import com.ziroh.infinity.vertex.InfinityVertex;

import java.util.Optional;

/**
 * Lambda that takes an image pixel matrix as input and returns a
 * black & white (grayscale) version of the same matrix.
 *
 * ASSUMPTION: each cell of the input matrix holds a single pixel packed
 * as a 24-bit RGB integer (0xRRGGBB) stored as a double, e.g. the value
 * produced by (r << 16) | (g << 8) | b. The output matrix has the exact
 * same dimensions, with each pixel replaced by its grayscale equivalent
 * (R = G = B = luminance), still packed the same way, so it can be fed
 * straight back into any downstream lambda expecting the same format.
 *
 * If your pipeline instead represents channels as separate matrices
 * (e.g. one matrix for R, one for G, one for B) or one channel per row,
 * only extractChannels()/packGray() need to change — the retrieval and
 * output-wrapping logic below stays identical.
 */
public class LambdaFilter extends LambdaBase {

    @Override
    public Optional<TypedInputBase<?, ?>> computeBiFunction(InfinityVertex infinityVertex, Object[] inputs) {

        try {
            InputMatrixMessage inputMatrixMessage = (InputMatrixMessage)
                    infinityVertex.findRetrieve(this.getInputDB(), this.getInputCollection(), inputs[0]);

            double[][] pixels = inputMatrixMessage.getValue();
            double[][] result = toGrayscale(pixels);

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

    public static LambdaFilter create(int noOfLayers, String lambdaId, String inputDB, String inputCollection, String outputDB, String outputCollection, String outputCategoryType, boolean returnOutput) {
        LambdaFilter lambda = new LambdaFilter();
        lambda.setLambdaId(lambdaId);
        lambda.setInputDB(inputDB);
        lambda.setInputCollection(inputCollection);
        lambda.setOutputDB(outputDB);
        lambda.setOutputCollection(outputCollection);
        lambda.setOutputCategoryType(outputCategoryType);
        lambda.setReturnOutput(returnOutput);
        return lambda;
    }

    // Converts a packed-RGB pixel matrix into a packed-RGB grayscale matrix,
    // preserving dimensions exactly.
    public static double[][] toGrayscale(double[][] pixels) {
        int rows = pixels.length;
        double[][] result = new double[rows][];

        for (int r = 0; r < rows; r++) {
            int cols = pixels[r].length;
            result[r] = new double[cols];
            for (int c = 0; c < cols; c++) {
                int[] channels = extractChannels((long) pixels[r][c]);
                int gray = luminance(channels[0], channels[1], channels[2]);
                result[r][c] = packGray(gray);
            }
        }
        return result;
    }

    // Unpacks a 0xRRGGBB long into {r, g, b}.
    private static int[] extractChannels(long packed) {
        int r = (int) ((packed >> 16) & 0xFF);
        int g = (int) ((packed >> 8) & 0xFF);
        int b = (int) (packed & 0xFF);
        return new int[]{r, g, b};
    }

    // Standard luminosity-method grayscale conversion, clamped to [0, 255].
    private static int luminance(int r, int g, int b) {
        int gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
        if (gray < 0) return 0;
        if (gray > 255) return 255;
        return gray;
    }

    // Repacks a single grayscale value as R = G = B = gray, 0xRRGGBB.
    private static double packGray(int gray) {
        int packed = (gray << 16) | (gray << 8) | gray;
        return (double) packed;
    }
}