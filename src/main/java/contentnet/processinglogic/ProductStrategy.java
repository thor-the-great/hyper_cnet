package contentnet.processinglogic;

import contentnet.ConceptnetAPI;
import contentnet.category.UNSPSCRecord;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;

import java.util.*;

import static contentnet.ResultProcessor.*;

public class ProductStrategy implements IStrategy {

    boolean IS_LOG_ENABLED;

    public ProductStrategy(boolean IS_LOG_ENABLED) {
        this.IS_LOG_ENABLED = IS_LOG_ENABLED;
    }

    @Override
    public Set<String> processWordsInternally(String farWord, String word, boolean isLowerTreeLevel, Set<String> categoryContext, UNSPSCRecord category) {
        Set<String> isAWords = null;
        if (isLowerTreeLevel) {
            //checking for parts - if processing word is part of the main word
            List<String> possibleProducts =  category.getAttributes().get(UNSPSCRecord._ATTR_NAME_PRODUCT);
            List<String> possibleAttribute =  category.getAttributes().get(UNSPSCRecord._ATTR_NAME_ATTRVALUE);
            boolean foundAtLeastOneMatch = isPartConnectedToBase(word, possibleProducts, false);
            if (!foundAtLeastOneMatch) {
                foundAtLeastOneMatch = isPartConnectedToBase(word, possibleAttribute, foundAtLeastOneMatch);
            }
            //checking if word isA main word
            if (!foundAtLeastOneMatch) {
                isAWords =
                        getInstance().extractEdgeEnds(
                                ConceptnetAPI.getInstance().getIsA(word), word);
                if (isAWords != null && isAWords.size() > 0) {
                    for (String isAWord: isAWords) {
                        if (possibleAttribute != null && possibleAttribute.contains(isAWord)) {
                            foundAtLeastOneMatch = true;
                            break;
                        }
                        if (possibleProducts != null && possibleProducts.contains(isAWord)) {
                            foundAtLeastOneMatch = true;
                            break;
                        }
                    }
                }
            }
            //checking complex terms
            if (!foundAtLeastOneMatch) {
                if (possibleProducts != null) {
                    for (String possibleProduct : possibleProducts) {
                        String newTerm = possibleProduct + "_" + word;
                        if (ConceptnetAPI.getInstance().isValidWord(newTerm)) {
                            foundAtLeastOneMatch = true;
                            break;
                        }
                    }
                }
                if (possibleAttribute != null) {
                    for (String possibleProduct : possibleAttribute) {
                        String newTerm = possibleProduct + "_" + word;
                        if (ConceptnetAPI.getInstance().isValidWord(newTerm)) {
                            foundAtLeastOneMatch = true;
                            break;
                        }
                    }
                }
            }
            if (!foundAtLeastOneMatch)
                return new HashSet<>();
        }

        Set<String> hasContextResultsForWord =
                getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getHasContext(word), word);
        //if (isProcessInContext && hasContextResultsForWord.size() > 0 ) {
        hasContextResultsForWord.removeIf(hasContextWord -> !categoryContext.contains(hasContextWord));
        if (hasContextResultsForWord.size() == 0) {
            //System.out.println("Word is in wrong contexts");
            return new HashSet<>();
        }

        //System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        if (isAWords == null)
            isAWords =
        //Set<String> isAWords =
                    getInstance().extractEdgeEnds(
                            ConceptnetAPI.getInstance().getIsA(word), word);

        //hyperResult.addAll(relatedResult);

        //System.out.println("--------------------------------");
        Map<String, Float> isARelationWeight = getInstance().getRelationWeight(word, isAWords);
        getInstance().processWordsWeights(word, isARelationWeight, new WeightProcessingDirectRelationStrategy(), IS_LOG_ENABLED);
        Set<String> isAFilteredWords = getInstance().adjustWordsPerWeights(isAWords, isARelationWeight);
        //System.out.println("--------------------------------");
        if (!farWord.equalsIgnoreCase(word)) {
            Map<String, Float> farRelationWeight = getInstance().getRelationWeight(farWord, isAWords);
            getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), IS_LOG_ENABLED);
            //Set<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
            isAFilteredWords = getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
        }

        for (Iterator<String> it = isAFilteredWords.iterator(); it.hasNext();) {
            String nextIsAWord = it.next();
            //for (int i = relatedToWords.size() - 1; i >= 0; i--) {
            //String nextRelatedToWord = relatedToWords.get(i);
            Set<String> isAWordContexts =
                    getInstance().extractEdgeEnds(
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

    private boolean isPartConnectedToBase(String word, List<String> possibleProducts, boolean foundAtLeastOneMatch) {
        if (possibleProducts == null)
            return false;
        for (String possibleProduct: possibleProducts) {
            if(!getInstance().isResultEmptyEdgesEnds(
                    ConceptnetAPI.getInstance().getHasA(possibleProduct, word))) {
                foundAtLeastOneMatch = true;
                break;
            }
            if (!foundAtLeastOneMatch) {
                if (!getInstance().isResultEmptyEdgesEnds(
                        ConceptnetAPI.getInstance().getAtLocation(possibleProduct, word))) {
                    foundAtLeastOneMatch = true;
                    break;
                }
            }
        }
        return foundAtLeastOneMatch;
    }
}
