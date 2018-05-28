package contentnet.weightprocessing;

import java.util.Map;

public interface IResultProcessingStrategy {

    Map<String, Float> processRelationWeights(Map<String, Float> dataToProcess);
}
