package contentnet.drivers.outdated;

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

    static final int MAX_RECUSRSION_LEVEL = 7;
    public static final int DELAY = Utils.DELAY;

    static final  boolean IS_LOG_ENABLED = false;

    static final String[] WORDS_SET1 = new String[]{
            "case", "cable", "mount", "adapter", "book", "camera", "background", "microphone", "dvd", "backpack",
            "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light", "bag",
            "plate", "monitor", "gobo", "bracket", "speaker", "box", "card", "strap", "lamp", "riflescope",
            "module", "paper", "controller", "amplifier", "battery", "headphone", "pedal", "screen", "binocular", "receiver",
            "transmitter", "tripod", "converter", "ring", "headset", "sleeve", "switch", "keyboard", "charger", "sunglass"
    };

    static final String[] WORDS_SET75 = new String[]{
            "case", "cable", "mount", "adapter", "book", "camera", "background", "microphone", "dvd", "backpack",
            "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light", "bag",
            "plate", "monitor", "gobo", "bracket", "speaker", "box", "card", "strap", "lamp", "riflescope",
            "module", "paper", "controller", "amplifier", "battery", "headphone", "pedal", "screen", "binocular", "receiver",
            "transmitter", "tripod", "converter", "ring", "headset", "sleeve", "switch", "keyboard", "charger", "sunglass",
            "string", "drive", "holder", "guitar", "connector", "clamp", "library", "knife", "plug",
            "control", "board", "supply", "film", "rack", "printer", "license", "switcher", "cart", "pack", "extender",
            "housing", "lectern", "projector", "sight", "interface", "processor", "cartridge", "mixer", "pouch", "protector"
    };

    static final String[] WORDS_SET2 = new String[]{
            //"case", "mount"
            //"mount", "tripod", "microphone"
            "case"
    };

    //static final String[] words = WORDS_SET75;
    static final String[] words = WORDS_SET2;

    public static void main(String[] args) {

        DriverHasContext driver = new DriverHasContext();
        driver.doWork();
    }

    void doWork() {
        long startTime = System.currentTimeMillis();

        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);

        //GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_2_CSV_FILE_PATH);
        if (!wordGraph.containsVertex(GraphUtils._CONCEPT_ROOT_NODE))
            wordGraph.addVertex(GraphUtils._CONCEPT_ROOT_NODE);

        for (String word: words ) {
            if  (!wordGraph.containsVertex(word)) {
                wordGraph.addVertex(word);
                processWords(wordGraph, word, word, 0);
            }
            System.out.println("Processed word : " + word);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (endTime - startTime)/ (1000) + " sec");

        //GraphUtils.exportGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);
        //GraphUtils.exportGraph(wordGraph, GraphUtils._INWORK_3_CSV_FILE_PATH);
        //GraphUtils.displayGraph(wordGraph);
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
                //wordGraph.addEdge(word, _CONCEPT_ROOT_NODE);
                if (!wordGraph.containsEdge(GraphUtils._CONCEPT_ROOT_NODE, word)) {
                    wordGraph.addEdge(GraphUtils._CONCEPT_ROOT_NODE, word);
                }
            }
            return recursionLevel;
        } else {
            //System.out.println("Bottom of recursion reached");
            //wordGraph.addEdge(word, _CONCEPT_ROOT_NODE);
            if (!wordGraph.containsEdge(GraphUtils._CONCEPT_ROOT_NODE, word)) {
                wordGraph.addEdge(GraphUtils._CONCEPT_ROOT_NODE, word);
            }
            return recursionLevel;
        }
    }

    private Set<String> processWordsInternally(String farWord, String word, boolean isProcessInContext) {
        //System.out.println("processing word '" + word + "' in context of far word '" + farWord + "'");
        Set<String> isAWords =
                ResultProcessor.getInstance().extractEdgeEnds(
                        ConceptnetAPI.getInstance().getIsARelated(word), word);
        //hyperResult = ResultProcessor.getInstance().sanitizeWordList(hyperResult, ongoingProcessedWords);

        //hyperResult.addAll(relatedResult);

        //System.out.println("--------------------------------");
        Map<String, Float> relationWeight = ResultProcessor.getInstance().getRelationWeight(word, isAWords);
        ResultProcessor.getInstance().processWordsWeights(word, relationWeight, new WeightProcessingDirectRelationStrategy(), IS_LOG_ENABLED);
        Set<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, relationWeight);
        //System.out.println("--------------------------------");
        if (!farWord.equalsIgnoreCase(word)) {
            Map<String, Float> farRelationWeight = ResultProcessor.getInstance().getRelationWeight(farWord, isAWords);
            ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), IS_LOG_ENABLED);
            //Set<String> filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
            filteredWords = ResultProcessor.getInstance().adjustWordsPerWeights(isAWords, farRelationWeight);
        }

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

        if (isProcessInContext) {
            Set<String> hasContextResults =
                    ResultProcessor.getInstance().extractEdgeEnds(
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
                ResultProcessor.getInstance().extractEdgeEnds(
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
                            ResultProcessor.getInstance().extractEdgeEnds(
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
