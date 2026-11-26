package mlpFeeders;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CustomMemory<K,V> {

    List<Map.Entry<K,V>> memory;

    public CustomMemory(){
        memory = new CopyOnWriteArrayList<>();
    }
}
