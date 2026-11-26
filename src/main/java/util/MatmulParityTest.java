package util;

import ai.djl.Device;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.ndarray.types.Shape;

import java.io.IOException;

public class MatmulParityTest {
    public static void main(String[] args) throws IOException {
        try (NDManager manager = NDManager.newBaseManager(Device.cpu())) {

            // Same values as the Python torch.manual_seed(0) run -- load from the .bin
            // files Python wrote (see the file-based test protocol from earlier), or
            // hardcode for a quick sanity check.
            NDArray a32 = loadBinFloat32(manager, "a.bin", new Shape(4, 4));
            NDArray b32 = loadBinFloat32(manager, "b.bin", new Shape(4, 4));

            // ---- the actual type conversion: FLOAT32 -> FLOAT16, same as PyTorch's .to(torch.float16) ----
            NDArray a16 = a32.toType(DataType.FLOAT16, false);
            NDArray b16 = b32.toType(DataType.FLOAT16, false);

            NDArray native16 = a16.matMul(b16);              // "native" fp16 matmul, mirrors a @ b
            NDArray out32 = native16.toType(DataType.FLOAT32, false);  // upcast back, mirrors .to(torch.float32)

            NDArray expected = loadBinFloat32(manager, "expected_out.bin", new Shape(4, 4));
            NDArray absDiff = out32.sub(expected).abs();

            System.out.println("Java result (fp16 matmul, upcast to fp32 for display):");
            System.out.println(out32);
            System.out.println("max abs diff vs Python: " + absDiff.max().getFloat());

            a32.close(); b32.close(); a16.close(); b16.close();
            native16.close(); out32.close(); expected.close(); absDiff.close();
        }
    }

    private static NDArray loadBinFloat32(NDManager manager, String path, Shape shape) throws java.io.IOException {
        byte[] bytes = java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(path));
        java.nio.FloatBuffer buf = java.nio.ByteBuffer.wrap(bytes)
                .order(java.nio.ByteOrder.LITTLE_ENDIAN).asFloatBuffer();
        return manager.create(buf, shape);
    }
}