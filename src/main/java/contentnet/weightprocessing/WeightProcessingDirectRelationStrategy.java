package contentnet.weightprocessing;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

public class WeightProcessingDirectRelationStrategy implements IResultProcessingStrategy {

    static final int MAX_DIRECT_CONNECTIONS = 10;

    @Override
    public Map<String, Float> processRelationWeights(Map<String, Float> dataToProcess) {
        float[] keyWeight = new float[dataToProcess.size()];
        int arrayCount = -1;
        Iterator<String> keys = dataToProcess.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            float weightResult = dataToProcess.get(key);
            if (weightResult <= 0.05f) {
                keys.remove();
            } else {
                arrayCount++;
                keyWeight[arrayCount] = weightResult;
            }
        }
        if (arrayCount >= MAX_DIRECT_CONNECTIONS) {
            Arrays.sort(keyWeight);
            keys = dataToProcess.keySet().iterator();
            while (keys.hasNext()) {
                String key = keys.next();
                float weightResult = dataToProcess.get(key);
                boolean found = false;
                for(int i = keyWeight.length - 1; i > keyWeight.length - (1 + MAX_DIRECT_CONNECTIONS);i--) {
                    if (weightResult == keyWeight[i]) {
                        found = true;
                        break;
                    }
                }
                if (!found)
                    keys.remove();
            }
        }
        return dataToProcess;
    }
}
