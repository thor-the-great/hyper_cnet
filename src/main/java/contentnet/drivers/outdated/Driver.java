package contentnet.drivers.outdated;

import contentnet.*;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.*;

public class Driver {

    static final int MAX_RECUSRSION_LEVEL = 5;

    public static void main(String[] args) {

        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        String farWord = "case";
        String word = "case";

        wordGraph.addVertex(word);

        Set<String> ongoingProcessedWords = new HashSet<>();

        processWords( wordGraph, farWord, word, 0, ongoingProcessedWords);

        System.out.println(wordGraph.toString());
    }

    private static int processWords(Graph<String, DefaultEdge> wordGraph, String farWord, String word, int recursionLevel, Set<String> ongoingProcessedWords) {
        if (recursionLevel < MAX_RECUSRSION_LEVEL) {
            recursionLevel++;
            Set<String> relatedWords = processWordsInternally(farWord, word, ongoingProcessedWords);
            if (relatedWords.size() > 0) {
                for (String relatedWord : relatedWords) {
                    wordGraph.addVertex(relatedWord);
                    wordGraph.addEdge(word, relatedWord);
                    Utils.doDelay(900);
                    processWords(wordGraph, farWord, Utils.getLabelFromConceptContextName(relatedWord), recursionLevel, ongoingProcessedWords);
                }
            }
            return recursionLevel;
        } else {
            System.out.println("Bottom of recursion reached");
            return recursionLevel;
        }
    }

    private static Set<String> processWordsInternally(String farWord, String word,  Set<String> ongoingProcessedWords) {
        System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        Set<String> hyperResult =
                ResultProcessor.getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getIsA(word), word);
        //hyperResult = ResultProcessor.getInstance().sanitizeWordList(hyperResult, ongoingProcessedWords);

        //hyperResult.addAll(relatedResult);

        //System.out.println("--------------------------------");
        Map<String, Float> relationWeight = ResultProcessor.getInstance().getRelationWeight(word, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, relationWeight, new WeightProcessingDirectRelationStrategy(), false);
        System.out.println("--------------------------------");
        ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, relationWeight);
        Map<String, Float> farRelationWeight = ResultProcessor.getInstance().getRelationWeight(farWord, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), true);
        Set<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, farRelationWeight);

        /*List<String> relatedResult =
                ResultProcessor.getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getRelatedTo(word), word);
        relatedResult = ResultProcessor.getInstance().sanitizeWordList(relatedResult, ongoingProcessedWords);
        Map<String, Float> weights = getRelationWeight(word, relatedResult);
        ResultProcessor.getInstance().processWordsWeights(word, weights, new WeightProcessingDirectRelationStrategy(), false);
        System.out.println("--------------------------------");
        ResultProcessor.getInstance().adjustWordsPerWeights(relatedResult, weights);
        farRelationWeight = getRelationWeight(farWord, relatedResult);
        ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), true);
        List<String> filteredRelatedWords = ResultProcessor.getInstance().adjustWordsPerWeights(relatedResult, farRelationWeight);

        filteredWords.addAll(filteredRelatedWords);*/

        return filteredWords;
//        return null;
    }

//    private static Map<String, Float> getRelationWeight(String mainWord, List<String> wordsInQuestion) {
//        Map<String, Float> wordsWeight = new HashMap();
//        for (String wordInQuestion: wordsInQuestion) {
//            float weightResult =
//                    ResultProcessor.getInstance().processRelationWeight(
//                            ConceptnetAPI.getInstance().getRelationWeight(mainWord, Utils.getLabelFromConceptContextName(wordInQuestion)));
//            wordsWeight.put(wordInQuestion, weightResult);
//            Utils.doDelay(900);
//        }
//        return wordsWeight;
//    }
}
