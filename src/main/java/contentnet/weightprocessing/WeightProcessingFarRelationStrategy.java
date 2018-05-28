package contentnet.weightprocessing;

import java.util.Iterator;
import java.util.Map;

public class WeightProcessingFarRelationStrategy implements IResultProcessingStrategy {

    @Override
    public Map<String, Float> processRelationWeights(Map<String, Float> dataToProcess) {
        Iterator<String> keys = dataToProcess.keySet().iterator();
        while (keys.hasNext()) {
            String key = keys.next();
            float weightResult = dataToProcess.get(key);
            if (weightResult < 0.05f) {
                keys.remove();
            }
        }
        return dataToProcess;
    }
}
