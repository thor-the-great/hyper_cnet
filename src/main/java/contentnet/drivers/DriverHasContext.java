package contentnet.drivers;

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.Utils;
import contentnet.weightprocessing.WeightProcessingContextRelationStrategy;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;
import org.jgrapht.Graph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import javax.swing.*;
import java.util.*;

public class DriverHasContext {

    static final int MAX_RECUSRSION_LEVEL = 3;
    public static final int DELAY = 850;
    static  final String CONCEPT_ROOT_NODE = "CONCEPT_ROOT_NODE";

    public static void main(String[] args) {

        DriverHasContext driver = new DriverHasContext();

        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        wordGraph.addVertex(CONCEPT_ROOT_NODE);

        String farWord = "case";
        String word = "case";
        wordGraph.addVertex(word);
        Set<String> ongoingProcessedWords = new HashSet<>();

        driver.processWords(wordGraph, farWord, word, 0, ongoingProcessedWords);

        driver.printGraph(wordGraph);

        driver.displayGraph(wordGraph);
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
                    wordGraph.addVertex(relatedWord);
                    //wordGraph.addEdge(word, relatedWord);
                    wordGraph.addEdge(relatedWord, word);
                    Utils.doDelay(DELAY);
                    processWords(wordGraph, farWord, Utils.getLabelFromConceptContextName(relatedWord), recursionLevel, ongoingProcessedWords);
                }
            }
            else {
                //wordGraph.addEdge(word, CONCEPT_ROOT_NODE);
                wordGraph.addEdge(CONCEPT_ROOT_NODE, word);
            }
            return recursionLevel;
        } else {
            //System.out.println("Bottom of recursion reached");
            //wordGraph.addEdge(word, CONCEPT_ROOT_NODE);
            wordGraph.addEdge(CONCEPT_ROOT_NODE, word);
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
        ResultProcessor.getInstance().processWordsWeights(word, relationWeight, new WeightProcessingDirectRelationStrategy(), false);
        //System.out.println("--------------------------------");
        ResultProcessor.getInstance().adjustWordsPerWeights(hyperResult, relationWeight);
        Map<String, Float> farRelationWeight = getRelationWeight(farWord, hyperResult);
        ResultProcessor.getInstance().processWordsWeights(word, farRelationWeight, new WeightProcessingFarRelationStrategy(), false);
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
                    ResultProcessor.getInstance().processWordsWeights(nextRelatedWord, contextWeight, new WeightProcessingContextRelationStrategy(), false);
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

    void printGraph(Graph<String, DefaultEdge> wordGraph) {
        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator<>(wordGraph);
        while (iterator.hasNext()) {
            String vertex = iterator.next();
            Set<DefaultEdge> edges = wordGraph.outgoingEdgesOf(vertex);
            for (DefaultEdge edge: edges) {
                System.out.println(wordGraph.getEdgeSource(edge) + "->" + wordGraph.getEdgeTarget(edge));
            }
        }
    }

    void displayGraph(Graph<String, DefaultEdge> wordGraph) {
        JFrame frame = new JFrame("Concept net word graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(wordGraph);

        mxIGraphLayout layout = new mxCircleLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        frame.add(new mxGraphComponent(graphAdapter));

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }
}
