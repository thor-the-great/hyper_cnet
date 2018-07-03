package contentnet.processinglogic;

import contentnet.ConceptnetAPI;
import contentnet.NltkAPI;
import contentnet.category.UNSPSCRecord;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;

import java.util.*;

import static contentnet.ResultProcessor.getInstance;

public class SmallProductStrategy implements IStrategy {

    boolean IS_LOG_ENABLED;

    public SmallProductStrategy(boolean IS_LOG_ENABLED) {
        this.IS_LOG_ENABLED = IS_LOG_ENABLED;
    }

    static Set<String> wordContextMap = new HashSet<>();

    @Override
    public Set<List<String>> processWordsInternally(String farWord, String word, boolean isLowerTreeLevel, Set<String> categoryContext, UNSPSCRecord category) {
        Set<String> isAWords = null;
        Set<String> matchedIsAWords = new HashSet<>();
        Set<List<String>> result = new HashSet<>();
        if (isLowerTreeLevel) {
            //checking for parts - if processing word is part of the main word
            List<String> possibleProducts =  category.getAttributes().get(UNSPSCRecord._ATTR_NAME_PRODUCT);
            List<String> possibleAttribute =  category.getAttributes().get(UNSPSCRecord._ATTR_NAME_ATTRVALUE);
            List<String> allPossibleAttributes = new ArrayList<>();
            if (possibleAttribute != null)
                allPossibleAttributes.addAll(possibleAttribute);
            if (possibleProducts != null)
                allPossibleAttributes.addAll(possibleProducts);
            //boolean foundAtLeastOneMatch = isPartConnectedToBase(word, allPossibleAttributes, false);
            //checking if word isA main word
            //if (!foundAtLeastOneMatch) {
                isAWords =
                        getInstance().extractEdgeEnds(
                                ConceptnetAPI.getInstance().getIsA(word), word);
                if (isAWords != null && isAWords.size() > 0) {
                    for (String isAWord: isAWords) {
                        if (allPossibleAttributes.contains(isAWord)) {
                            //foundAtLeastOneMatch = true;
                            matchedIsAWords.add(isAWord);
                            //break;
                        }
                    }
                }
            //}
            //checking complex terms
            //if (!foundAtLeastOneMatch) {
                for (String possibleProductAttribute : allPossibleAttributes) {
                    String newTerm = possibleProductAttribute + "_" + word;
                    if (ConceptnetAPI.getInstance().isValidWord(newTerm)) {
                        //foundAtLeastOneMatch = true;
                        matchedIsAWords.add(newTerm);
                        //break;
                    }
                }
            //}
            if (matchedIsAWords.size() > 0) {

                for (String matchedWord : matchedIsAWords) {
                    List<String> returnList = addWordOrWordRootToResult(result, matchedWord);
                    String addedWord = returnList.get(0);
                    addWordAndSynonyms(categoryContext, returnList, addedWord);
                }
                //return result;
            }
            //if (!foundAtLeastOneMatch) {
                //try nltk approach
                /*Set<List<String>> result = new HashSet<>();
                StringBuilder sb = new StringBuilder();
                categoryContext.forEach(categoryContextString -> {sb.append(categoryContextString).append(";");});
                for (String isAWord: isAWords) {
                    float similarity = NltkAPI.getInstance().getHighestSimilarity(isAWord, sb.toString());
                    float thresholdSimilarity = 0.2f;
                    if (similarity >= thresholdSimilarity) {
                        List<String> returnList = addWordOrWordRootToResult(result, isAWord);
                        String addedWord = returnList.get(0);
                        addWordAndSynonyms(categoryContext, returnList, addedWord);
                    }
                }
                //return new HashSet<>();
                return result;*/
            //}*/
        }

        Set<String> contextResultsForWord =
                getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getWordContext(word), word);
//        if (wordContextMap.contains(word))
//            System.out.println("Cache hit");
//        else
//            wordContextMap.add(word);
        //if (isProcessInContext && hasContextResultsForWord.size() > 0 ) {
        contextResultsForWord.removeIf(hasContextWord -> !categoryContext.contains(hasContextWord));
        if (contextResultsForWord.size() == 0) {
            //test approach - calling wordnet nltk for lowest common hypernym
            if (matchedIsAWords.size() > 0 ) {
                //Set<List<String>> result = new HashSet<>();
                StringBuilder sb = new StringBuilder();
                categoryContext.forEach(categoryContextString -> {sb.append(categoryContextString).append(";");});
                for (String matchedIsAWord : matchedIsAWords) {
                    List<String> lchResult = NltkAPI.getInstance().getLowestCommonHypernyms(matchedIsAWord, word);
                    for (String lowestCommonHypermyn: lchResult) {
                        float similarity = NltkAPI.getInstance().getHighestSimilarity(lowestCommonHypermyn, sb.toString());
                        float thresholdSimilarity = 0.12f;
                        if (similarity >= thresholdSimilarity) {
                            List<String> returnList = addWordOrWordRootToResult(result, matchedIsAWord);
                            String addedWord = returnList.get(0);
                            addWordAndSynonyms(categoryContext, returnList, addedWord);
                        }
                    }
                }
                return result;
            }
            return new HashSet<>();
        }

        //System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        if (isAWords == null)
            isAWords =
        //Set<String> isAWords =
                    getInstance().extractEdgeEnds(
                            ConceptnetAPI.getInstance().getIsA(word), word);

        Map<String, Float> isARelationWeight = getInstance().getRelationWeight(word, isAWords);
        getInstance().processWordsWeights(word, isARelationWeight, new WeightProcessingDirectRelationStrategy(), IS_LOG_ENABLED);
        Set<String> isAFilteredWords = getInstance().adjustWordsPerWeights(isAWords, isARelationWeight);
        if (!farWord.equalsIgnoreCase(word)) {
            Map<String, Float> farRelationWeight = getInstance().getRelationWeight(farWord, isAWords);
            getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), IS_LOG_ENABLED);
            isAFilteredWords = getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
        }

        for (Iterator<String> it = isAFilteredWords.iterator(); it.hasNext();) {
            String nextIsAWord = it.next();
            Set<String> isAWordContexts =
                    getInstance().extractEdgeEnds(
                            ConceptnetAPI.getInstance().getWordContext(nextIsAWord), nextIsAWord);
//            if (wordContextMap.contains(nextIsAWord))
//                System.out.println("Cache hit");
//            else
//                wordContextMap.add(nextIsAWord);
            boolean isContextMatchFound = false;
            //if there are word contexts - deal with them
            if (isAWordContexts.size() == 0) {
                //test approach - calling wordnet nltk for lowest common hypernym
                //boolean isContextRelevant = false;
                List<String> lchResult = NltkAPI.getInstance().getLowestCommonHypernyms(nextIsAWord, word);
                for (String lowestCommonHypermyn: lchResult) {
                    Set<String> hasContextResultsForLch =
                            getInstance().extractEdgeEnds(
                                    ConceptnetAPI.getInstance().getWordContext(lowestCommonHypermyn), lowestCommonHypermyn);
//                    if (wordContextMap.contains(lowestCommonHypermyn))
//                        System.out.println("Cache hit");
//                    else
//                        wordContextMap.add(lowestCommonHypermyn);
                    for (String contextOfLch: hasContextResultsForLch) {
                        if (categoryContext.contains(contextOfLch)) {
                            isContextMatchFound = true;
                            break;
                        }
                    }
                    if(isContextMatchFound)
                        break;
                }
                //relatedToWords.remove(i);
                if(!isContextMatchFound) {
                    it.remove();
                    continue;
                }
            }//if word doesn't have context - try to get something anyway
            else {
                for (String isAWordContext : isAWordContexts) {
                    if (categoryContext.contains(isAWordContext)) {
                        isContextMatchFound = true;
                        break;
                    }
                }
            }
            if (!isContextMatchFound) {
                //relatedToWords.remove(i);
                it.remove();
            }
        }
        //Set<List<String>> result = new HashSet<>();
        for (String resultWord: isAFilteredWords) {
            List<String> returnList = addWordToResult(result, resultWord);
            //List<String> returnList = addWordOrWordRootToResult(result, matchedIsAWord);
            String addedWord = returnList.get(0);
            addWordAndSynonyms(categoryContext, returnList, addedWord);
        }
        return result;
    }

    private void addWordAndSynonyms(Set<String> categoryContext, List<String> returnList, String addedWord) {
        Set<String> synonyms = ConceptnetAPI.getInstance().getSynonyms(addedWord);
        if (synonyms.size() > 0 ) {
            for (String synonymWord : synonyms) {
                //check that it's ok synonym
                Set<String> synonymContexts =
                        getInstance().extractEdgeEnds(
                                ConceptnetAPI.getInstance().getWordContext(synonymWord), synonymWord);
//                if (wordContextMap.contains(synonymWord))
//                    System.out.println("Cache hit");
//                else
//                    wordContextMap.add(synonymWord);
                //check by context match
                if (synonymContexts != null && synonymContexts.size() > 0) {
                    boolean contextMatched = false;
                    for (String synonymContext: synonymContexts ) {
                        if (categoryContext.contains(synonymContext)) {
                            contextMatched = true;
                            break;
                        }
                    }
                    if (contextMatched) {
                        returnList.add(synonymWord);
                        return;
                    }
                }
                //no contexts - check direct lower common hypernym
                List<String> lchResult = NltkAPI.getInstance().getLowestCommonHypernyms(synonymWord, addedWord);
                if (lchResult.contains(synonymWord) || lchResult.contains(addedWord)) {
                    returnList.add(synonymWord);
                    return;
                }
            }
        }
    }

    private List<String> addWordOrWordRootToResult(Set<List<String>> result, String isAWord) {
        Set<String> wordRoots = ConceptnetAPI.getInstance().getWordRoots(isAWord);
        if (wordRoots.size() == 0) {
            //result.add(matchedIsAWord);
            return addWordToResult(result, isAWord);
        }
        else {
            String wordRoot = wordRoots.iterator().next();
            return addWordToResult(result, wordRoot);
        }
    }

    private List<String> addWordToResult(Set<List<String>> result, String isAWord) {
        List<String> words = new ArrayList<>();
        words.add(isAWord);
        result.add(words);
        return words;
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
