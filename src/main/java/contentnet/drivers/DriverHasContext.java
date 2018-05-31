package contentnet.drivers;

import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.Utils;
import contentnet.graph.GraphUtils;
import contentnet.weightprocessing.WeightProcessingContextRelationStrategy;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;
import contentnet.weightprocessing.WeightProcessingRelatedToStrategy;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import java.util.*;

public class DriverHasContext {

    static final int MAX_RECUSRSION_LEVEL = 2;
    public static final int DELAY = 950;
    static  final String CONCEPT_ROOT_NODE = "CONCEPT_ROOT_NODE";
    static final  boolean IS_LOG_ENABLED = true;

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();

        DriverHasContext driver = new DriverHasContext();

        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        //GraphUtils.importGraph(wordGraph);
        //String farWord = "";
        //String word = "";
        Set<String> ongoingProcessedWords = new HashSet<>();
        if (!wordGraph.containsVertex(CONCEPT_ROOT_NODE))
            wordGraph.addVertex(CONCEPT_ROOT_NODE);


        /*farWord = "case";
        if  (!wordGraph.containsVertex(farWord))
            wordGraph.addVertex(farWord);
        driver.processWords(wordGraph, farWord, farWord, 0, ongoingProcessedWords);
        GraphUtils.printGraph(wordGraph);*/

        String[] words = new String[]{
                "case"
                //"case", "cable", "mount", "adapter", "book"
                //"case", "cable", "mount", "adapter", "book", "camera", "background", "microphone", "dvd", "backpack"
                //"filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light", "bag"
        };
        for (String word: words ) {
            if  (!wordGraph.containsVertex(word))
                wordGraph.addVertex(word);
            driver.processWords(wordGraph, word, word, 0);
            //GraphUtils.printGraph(wordGraph);
        }

        /*farWord = "cable";
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

        //GraphUtils.importGraph(wordGraph);

        /*String[] words = new String[]{
                "adapter", "book", "camera", "background", "microphone", "dvd", "backpack", "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light"
        };
        for (String wordToProcess: words ) {
            farWord = wordToProcess; word = farWord;
            if  (!wordGraph.containsVertex(word))
                wordGraph.addVertex(word);
            driver.processWords(wordGraph, farWord, word, 0, ongoingProcessedWords);
            GraphUtils.printGraph(wordGraph);
        }*/

        long endTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (endTime - startTime)/ (1000) + " sec");

        //GraphUtils.exportGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);
        GraphUtils.displayGraph(wordGraph);
    }

    int processWords(Graph<String, DefaultEdge> wordGraph, String farWord, String word, int recursionLevel) {
        if (recursionLevel < MAX_RECUSRSION_LEVEL) {
            recursionLevel++;
            boolean isProcessContext = false;
            if (recursionLevel == 1)
                isProcessContext = true;
            Set<String> relatedWords = processWordsInternally(farWord, word, isProcessContext);
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
                    processWords(wordGraph, farWord, Utils.getLabelFromConceptContextName(relatedWord), recursionLevel);
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

    private Set<String> processWordsInternally(String farWord, String word, boolean isProcessInContext) {
        //System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        Set<String> hyperResult =
                ResultProcessor.getInstance().processHypernyms(
                        ConceptnetAPI.getInstance().getHypernyms(word), word);
        //hyperResult = ResultProcessor.getInstance().sanitizeWordList(hyperResult, ongoingProcessedWords);

        //hyperResult.addAll(relatedResult);

        //System.out.println("--------------------------------");
        Map<String, Float> relationWeight = ResultProcessor.getInstance().getRelationWeight(word, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, relationWeight, new WeightProcessingDirectRelationStrategy(), IS_LOG_ENABLED);
        //System.out.println("--------------------------------");
        ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, relationWeight);
        Map<String, Float> farRelationWeight = ResultProcessor.getInstance().getRelationWeight(farWord, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), IS_LOG_ENABLED);
        Set<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, farRelationWeight);

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
            Set<String> hasContextResults =
                    ResultProcessor.getInstance().processHypernyms(
                            ConceptnetAPI.getInstance().getHasContext(word), word);
            /*for (int i = hasContextResults.size() - 1; i >= 0; i--) {
                if (!ConceptnetAPI.ALLOWED_CONTEXT.contains(hasContextResults.get(i))) {
                    hasContextResults.remove(i);
                }
            }*/
            for (Iterator<String> it = hasContextResults.iterator(); it.hasNext();) {
                String hasContextWord = it.next();
                if (!ConceptnetAPI.ALLOWED_CONTEXT.contains(hasContextWord)) {
                    it.remove();
                }
            }
            if (hasContextResults.size() > 0 ) {
            /*for (String contextOf : hasContextResults) {
                System.out.println("Context: " + word + " => " + contextOf);
            }*/
                TreeMap<Float, String> contextPriorityWords = new TreeMap<>();
                //for (int i = filteredWords.size() - 1; i >= 0; i--) {
                for (String nextRelatedWord: filteredWords) {
                    //String nextRelatedWord = filteredWords.get(i);
                    Map<String, Float> contextWeight = ResultProcessor.getInstance().getRelationWeight(nextRelatedWord, hasContextResults);
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
                //for (int i = filteredWords.size() - 1; i >= 0; i--) {
                for ( Iterator<String> it = filteredWords.iterator(); it.hasNext();) {
                    String filteredWord = it.next();
                    if (!passedPriorityContextWords.contains(filteredWord)) {
                    //if (!passedPriorityContextWords.contains(filteredWords.get(i))) {
                        //System.out.println("Word '" + filteredWords.get(i) + "' removed due to context mismatch");
                        //filteredWords.remove(i);
                        it.remove();
                    }
                }
            }

            //added relatedTo
            Utils.doDelay(DELAY);
            Set<String> relatedToWords =
                ResultProcessor.getInstance().processHypernyms(
                    ConceptnetAPI.getInstance().getRelatedTo(word), word);
            //relatedToWords = ResultProcessor.getInstance().sanitizeWordList(relatedToWords, ongoingProcessedWords);

            Map<String, Float> relatedToWordsWeight = ResultProcessor.getInstance().getRelationWeight(word, relatedToWords);
            ResultProcessor.getInstance().processWordsWeights(word, relatedToWordsWeight, new WeightProcessingRelatedToStrategy(), IS_LOG_ENABLED);
            ResultProcessor.getInstance().adjustWordsPerWeights(relatedToWords, relatedToWordsWeight);
            //after this we have relateToWords that are postprocessed - no negative weight, first N taken

            //now search related from the same context as main word
            if (hasContextResults.size() > 0 ) {
            /*for (String contextOf : hasContextResults) {
                System.out.println("Context: " + word + " => " + contextOf);
            }*/
                for (Iterator<String> it = relatedToWords.iterator(); it.hasNext();) {
                    String nextRelatedToWord = it.next();
                //for (int i = relatedToWords.size() - 1; i >= 0; i--) {
                    //String nextRelatedToWord = relatedToWords.get(i);
                    Utils.doDelay(DELAY);
                    Set<String> relatedToWordContexts =
                            ResultProcessor.getInstance().processHypernyms(
                                    ConceptnetAPI.getInstance().getHasContext(nextRelatedToWord), nextRelatedToWord);
                    if (relatedToWordContexts.size() == 0) {
                        //relatedToWords.remove(i);
                        it.remove();
                        continue;
                    }
                    boolean isContextMatchFound = false;
                    for (String mainWordContext : hasContextResults) {
                        if (relatedToWordContexts.contains(mainWordContext)) {
                            isContextMatchFound = true;
                            break;
                        }
                    }
                    if (!isContextMatchFound) {
                        //relatedToWords.remove(i);
                        it.remove();
                    }
                }
            }
            filteredWords.addAll(relatedToWords);
        }
        return filteredWords;
    }
}
