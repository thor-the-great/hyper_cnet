package contentnet.processinglogic;

import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.category.UNSPSCRecord;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;

import java.util.*;

public class ProductStrategy implements IStrategy {

    boolean IS_LOG_ENABLED;

    public ProductStrategy(boolean IS_LOG_ENABLED) {
        this.IS_LOG_ENABLED = IS_LOG_ENABLED;
    }

    @Override
    public Set<String> processWordsInternally(String farWord, String word, boolean isLowerTreeLevel, Set<String> categoryContext, UNSPSCRecord category) {
        if (isLowerTreeLevel) {
            List<String> possibleProducts =  category.getAttributes().get(UNSPSCRecord._ATTR_NAME_PRODUCT);
            boolean foundAtLeastOneMatch = false;
            for (String possibleProduct: possibleProducts) {
                Set<String> hasAResults =
                        ResultProcessor.getInstance().extractEdgeEnds(
                                ConceptnetAPI.getInstance().getHasA(possibleProduct, word), word);
                if(hasAResults.size() > 0) {
                    foundAtLeastOneMatch = true;
                    break;
                }
                if (!foundAtLeastOneMatch) {
                    Set<String> atLocationResults =
                            ResultProcessor.getInstance().extractEdgeEnds(
                                    ConceptnetAPI.getInstance().getAtLocation(possibleProduct, word), word);
                    if (atLocationResults.size() > 0) {
                        foundAtLeastOneMatch = true;
                        break;
                    }
                }
            }
            if (!foundAtLeastOneMatch)
                return new HashSet<>();
        }

        Set<String> hasContextResultsForWord =
                ResultProcessor.getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getHasContext(word), word);
        //if (isProcessInContext && hasContextResultsForWord.size() > 0 ) {
        hasContextResultsForWord.removeIf(hasContextWord -> !categoryContext.contains(hasContextWord));
        if (hasContextResultsForWord.size() == 0) {
            //System.out.println("Word is in wrong contexts");
            return new HashSet<>();
        }

        //System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        Set<String> isAWords =
                ResultProcessor.getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getIsARelated(word), word);

        //hyperResult.addAll(relatedResult);

        //System.out.println("--------------------------------");
        Map<String, Float> isARelationWeight = ResultProcessor.getInstance().getRelationWeight(word, isAWords);
        ResultProcessor.getInstance().processWordsWeights(word, isARelationWeight, new WeightProcessingDirectRelationStrategy(), IS_LOG_ENABLED);
        Set<String> isAFilteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, isARelationWeight);
        //System.out.println("--------------------------------");
        if (!farWord.equalsIgnoreCase(word)) {
            Map<String, Float> farRelationWeight = ResultProcessor.getInstance().getRelationWeight(farWord, isAWords);
            ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), IS_LOG_ENABLED);
            //Set<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
            isAFilteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
        }

        for (Iterator<String> it = isAFilteredWords.iterator(); it.hasNext();) {
            String nextIsAWord = it.next();
            //for (int i = relatedToWords.size() - 1; i >= 0; i--) {
            //String nextRelatedToWord = relatedToWords.get(i);
            Set<String> isAWordContexts =
                    ResultProcessor.getInstance().extractEdgeEnds(
                            ConceptnetAPI.getInstance().getHasContext(nextIsAWord), nextIsAWord);
            if (isAWordContexts.size() == 0) {
                //relatedToWords.remove(i);
                it.remove();
                continue;
            }
            boolean isContextMatchFound = false;
            for (String isAWordContext : isAWordContexts) {
                if (categoryContext.contains(isAWordContext)) {
                    isContextMatchFound = true;
                    break;
                }
            }
            if (!isContextMatchFound) {
                //relatedToWords.remove(i);
                it.remove();
            }
        }

        return isAFilteredWords;
    }
}
