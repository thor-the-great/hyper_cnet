package contentnet.drivers;

import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.Utils;
import contentnet.graph.GraphUtils;
import contentnet.weightprocessing.WeightProcessingContextRelationStrategy;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import java.util.*;

public class DriverHasContext {

    static final int MAX_RECUSRSION_LEVEL = 8;
    public static final int DELAY = 850;
    static  final String CONCEPT_ROOT_NODE = "CONCEPT_ROOT_NODE";
    static final  boolean IS_LOG_ENABLED = false;

    public static void main(String[] args) {

        DriverHasContext driver = new DriverHasContext();

        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        String farWord = "";
        String word = "";
        Set<String> ongoingProcessedWords = new HashSet<>();
        /*wordGraph.addVertex(CONCEPT_ROOT_NODE);


        /*farWord = "case";
        word = "case";
        if  (!wordGraph.containsVertex(word))
            wordGraph.addVertex(word);
        driver.processWords(wordGraph, farWord, word, 0, ongoingProcessedWords);
        GraphUtils.printGraph(wordGraph);

        farWord = "cable";
        word = "cable";
        if (!wordGraph.containsVertex(word))
            wordGraph.addVertex(word);
        driver.processWords(wordGraph, farWord, word, 0, ongoingProcessedWords);
        GraphUtils.printGraph(wordGraph);

        farWord = "mount";
        word = "mount";
        if (!wordGraph.containsVertex(word))
            wordGraph.addVertex(word);
        driver.processWords(wordGraph, farWord, word, 0, ongoingProcessedWords);
        GraphUtils.printGraph(wordGraph);

        //driver.displayGraph(wordGraph);

        GraphUtils.exportGraph(wordGraph, "C:\\work\\ariba\\wordGraph.csv");*/

        GraphUtils.importGraph(wordGraph);

        String[] words = new String[]{
                "adapter", "book", "camera", "background", "microphone", "dvd", "backpack", "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light"
        };
        for (String wordToProcess: words ) {
            farWord = wordToProcess; word = farWord;
            if  (!wordGraph.containsVertex(word))
                wordGraph.addVertex(word);
            driver.processWords(wordGraph, farWord, word, 0, ongoingProcessedWords);
            GraphUtils.printGraph(wordGraph);
        }

        GraphUtils.exportGraph(wordGraph, GraphUtils.DEFAULT_CSV_FILE_PATH);
    }

    int processWords(Graph<String, DefaultEdge> wordGraph, String farWord, String word, int recursionLevel, Set<String> ongoingProcessedWords) {
        if (recursionLevel < MAX_RECUSRSION_LEVEL) {
            recursionLevel++;
            boolean isProcessContext = false;
            if (recursionLevel == 1)
                isProcessContext = true;
            List<String> relatedWords = processWordsInternally(farWord, word, ongoingProcessedWords, isProcessContext);
            if (relatedWords.size() > 0) {
                for (String relatedWord : relatedWords) {
                    if (!wordGraph.containsVertex(relatedWord)) {
                        wordGraph.addVertex(relatedWord);
                        //wordGraph.addEdge(word, relatedWord);
                    }
                    if (!wordGraph.containsEdge(relatedWord, word)) {
                        wordGraph.addEdge(relatedWord, word);
                    }
                    Utils.doDelay(DELAY);
                    processWords(wordGraph, farWord, Utils.getLabelFromConceptContextName(relatedWord), recursionLevel, ongoingProcessedWords);
                }
            }
            else {
                //wordGraph.addEdge(word, CONCEPT_ROOT_NODE);
                if (!wordGraph.containsEdge(CONCEPT_ROOT_NODE, word)) {
                    wordGraph.addEdge(CONCEPT_ROOT_NODE, word);
                }
            }
            return recursionLevel;
        } else {
            //System.out.println("Bottom of recursion reached");
            //wordGraph.addEdge(word, CONCEPT_ROOT_NODE);
            if (!wordGraph.containsEdge(CONCEPT_ROOT_NODE, word)) {
                wordGraph.addEdge(CONCEPT_ROOT_NODE, word);
            }
            return recursionLevel;
        }
    }

    private List<String> processWordsInternally(String farWord, String word,  Set<String> ongoingProcessedWords, boolean isProcessInContext) {
        //System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        List<String> hyperResult =
                ResultProcessor.getInstance().processHypernyms(
                        ConceptnetAPI.getInstance().getHypernyms(word), word);
        hyperResult = ResultProcessor.getInstance().sanitizeWordList(hyperResult, ongoingProcessedWords);

        //hyperResult.addAll(relatedResult);

        //System.out.println("--------------------------------");
        Map<String, Float> relationWeight = getRelationWeight(word, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, relationWeight, new WeightProcessingDirectRelationStrategy(), IS_LOG_ENABLED);
        //System.out.println("--------------------------------");
        ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, relationWeight);
        Map<String, Float> farRelationWeight = getRelationWeight(farWord, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), IS_LOG_ENABLED);
        List<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, farRelationWeight);

        /*List<String> relatedResult =
                ResultProcessor.getInstance().processHypernyms(
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

        if (isProcessInContext) {
            List<String> hasContextResuts =
                    ResultProcessor.getInstance().processHypernyms(
                            ConceptnetAPI.getInstance().getHasContext(word), word);
            for (int i = hasContextResuts.size() - 1; i >= 0; i--) {
                if (!ConceptnetAPI.ALLOWED_CONTEXT.contains(hasContextResuts.get(i))) {
                    hasContextResuts.remove(i);
                }
            }
            if (hasContextResuts.size() > 0 ) {
            /*for (String contextOf : hasContextResuts) {
                System.out.println("Context: " + word + " => " + contextOf);
            }*/
                TreeMap<Float, String> contextPriorityWords = new TreeMap<>();
                for (int i = filteredWords.size() - 1; i >= 0; i--) {
                    String nextRelatedWord = filteredWords.get(i);
                    Map<String, Float> contextWeight = getRelationWeight(nextRelatedWord, hasContextResuts);
                    ResultProcessor.getInstance().processWordsWeights(nextRelatedWord, contextWeight, new WeightProcessingContextRelationStrategy(), IS_LOG_ENABLED);
                    for (String wordMatchesContext : contextWeight.keySet()) {
                        contextPriorityWords.put(contextWeight.get(wordMatchesContext), nextRelatedWord);
                    }

                /*if (contextWeight.size() == 0) {
                    filteredWords.remove(i);
                    System.out.println("Word '" + nextRelatedWord + "' removed due to context mismatch");
                }*/
                }
                int count = 0;
                List<String> passedPriorityContextWords = new ArrayList<>();
                for (Float weight : contextPriorityWords.descendingKeySet()) {
                    if (count < 2) {
                        passedPriorityContextWords.add(contextPriorityWords.get(weight));
                        count++;
                    } else
                        break;
                }
                for (int i = filteredWords.size() - 1; i >= 0; i--) {
                    if (!passedPriorityContextWords.contains(filteredWords.get(i))) {
                        //System.out.println("Word '" + filteredWords.get(i) + "' removed due to context mismatch");
                        filteredWords.remove(i);
                    }
                }
            }
        }
        return filteredWords;
    }

    Map<String, Float> getRelationWeight(String mainWord, List<String> wordsInQuestion) {
        Map<String, Float> wordsWeight = new HashMap();
        for (String wordInQuestion: wordsInQuestion) {
            float weightResult =
                    ResultProcessor.getInstance().processRelationWeight(
                            ConceptnetAPI.getInstance().getRelationWeight(mainWord, Utils.getLabelFromConceptContextName(wordInQuestion)));
            wordsWeight.put(wordInQuestion, weightResult);
            Utils.doDelay(DELAY);
        }
        return wordsWeight;
    }
}
