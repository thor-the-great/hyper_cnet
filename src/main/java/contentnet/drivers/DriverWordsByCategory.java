package contentnet.drivers;

import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.Utils;
import contentnet.category.UNSPSCRecord;
import contentnet.data.WordProvider;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphAPI;
import contentnet.graph.GraphUtils;
import contentnet.processinglogic.GeneralStrategy;
import contentnet.processinglogic.IStrategy;
import contentnet.processinglogic.ProductStrategy;
import contentnet.weightprocessing.WeightProcessingDirectRelationStrategy;
import contentnet.weightprocessing.WeightProcessingFarRelationStrategy;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.*;

/**
 * Build h/s graph for provided set of words (product semantics) as per provided context and build edges for provided
 * categories
 */
public class DriverWordsByCategory {

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

    static final String[] WORDS_SET100 = new String[]{
            "case", "cable", "mount", "adapter", "book", "camera", "background", "microphone", "dvd", "backpack",
            "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light", "bag",
            "plate", "monitor", "gobo", "bracket", "speaker", "box", "card", "strap", "lamp", "riflescope",
            "module", "paper", "controller", "amplifier", "battery", "headphone", "pedal", "screen", "binocular", "receiver",
            "transmitter", "tripod", "converter", "ring", "headset", "sleeve", "switch", "keyboard", "charger", "sunglass",
            "string", "drive", "holder", "guitar", "connector", "clamp", "library", "knife", "plug",
            "control", "board", "supply", "film", "rack", "printer", "license", "switcher", "cart", "pack", "extender",
            "housing", "lectern", "projector", "sight", "interface", "processor", "cartridge", "mixer", "pouch", "protector",
            "upgrade", "display", "clip", "arm", "support", "unsure",  "base", "station", "head", "bundle",
            "cap", "telescope", "windscreen", "audio", "enclosure", "transceiver", "loudspeaker", "antenna", "roll", "player"
    };

    static final String[] WORDS_SET2 = new String[]{
            //"case", "mount"
            //"mount", "tripod", "microphone"
            //"ipad"
            "background", "notebook", "server", "interface", "string", "keyboard", "monitor", "player", "cap", "connector", "dvd", "cover", "software"
    };

    //static final String[] words = WORDS_SET100;
    //static final String[] words = WORDS_SET2;
    //static final String[] words = WORDS_SET100;
    static final String[] words = WordProvider.getAllProductSemanticWords(500).toArray(new String[0]);
    //String unspscCategoryName = "Computers";
    //String unspscCategory = "432115";
    public static final Set<String> UNSPSC_CATEGORY_CONTEXT = new HashSet<String>() {
        {
            //add("unix");
            //add("microsoft_windows");
            add("computer");
            add("computer_hardware");
            add("software");
            add("computing");

            add("computer_science");
        }
    };


    public static void main(String[] args) {
        DriverWordsByCategory driver = new DriverWordsByCategory();
        /*Map<String, String> unspscDetails = new HashMap<>();
        unspscDetails.put(UNSPSCRecord._DETAIL_NAME_CATEGORY, "432115");
        unspscDetails.put(UNSPSCRecord._DETAIL_NAME_CATEGORY_NAME, "Computers");*/
        UNSPSCRecord category = new UNSPSCRecord();
        category.setUnspsc("432115");
        category.setUnspscName("Computers");
        List<String> attributes = new ArrayList<>();
        attributes.add("computer");
        category.getAttributes().put(UNSPSCRecord._ATTR_NAME_PRODUCT, attributes);
        driver.doWork(words, UNSPSC_CATEGORY_CONTEXT, category);
    }

    //Graph<String, ConceptEdge> doWork(String[] words, Set<String> categoryContext, Map<String, String> unspscDetails) {
    Graph<String, ConceptEdge> doWork(String[] words, Set<String> categoryContext, UNSPSCRecord categoryRecord) {
        return doWork(words, categoryContext, categoryRecord, null);
    }

    //Graph<String, ConceptEdge> doWork(String[] words, Set<String> categoryContext, Map<String, String> unspscDetails, Graph<String, ConceptEdge> wordGraph) {
    Graph<String, ConceptEdge> doWork(String[] words, Set<String> categoryContext, UNSPSCRecord categoryRecord, Graph<String, ConceptEdge> wordGraph) {
        long startTime = System.currentTimeMillis();

        if (wordGraph == null)
            wordGraph = new DirectedMultigraph<>(ConceptEdge.class);
        //GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_2_CSV_FILE_PATH);
        if (!wordGraph.containsVertex(GraphUtils._CONCEPT_ROOT_NODE)) {
            wordGraph.addVertex(GraphUtils._CONCEPT_ROOT_NODE);
        }

        for (String word: words ) {
            if  (!wordGraph.containsVertex(word)) {
                wordGraph.addVertex(word);
                processWords(wordGraph, word, word, 0, categoryContext, categoryRecord);
                GraphAPI.getInstance().fixCycles(categoryRecord, wordGraph);
            }
            //System.out.println("Processed word : " + word);
        }
        //check and remove direct parent of parent node
        List<String> vertexesToRemove = new ArrayList<>();
        for (Iterator<String> it = wordGraph.vertexSet().iterator(); it.hasNext();) {
            String vertex = it.next();
            if (wordGraph.outDegreeOf(vertex) == 0 && wordGraph.inDegreeOf(vertex) == 1) {
                Set<ConceptEdge> edges = wordGraph.incomingEdgesOf(vertex);
                ConceptEdge edge = edges.iterator().next();
                if (GraphUtils._CONCEPT_ROOT_NODE.equalsIgnoreCase(edge.getSource())) {
                    vertexesToRemove.add(vertex);
                }
            }
        }
        for (String vertex : vertexesToRemove) {
            wordGraph.removeAllEdges(GraphUtils._CONCEPT_ROOT_NODE, vertex);
            wordGraph.removeVertex(vertex);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (endTime - startTime)/ (1000) + " sec");

        //GraphUtils.exportGraph("C:\\work\\ariba\\export_test.csv", wordGraph);
        //GraphUtils.exportGraph(wordGraph, GraphUtils._INWORK_3_CSV_FILE_PATH);
        //GraphUtils.exportGraph("C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\wordGraph_100_words_in_work.csv", wordGraph);
        //GraphUtils.printGraphNew(wordGraph);
        GraphUtils.displayGraph(wordGraph);
        return wordGraph;
    }

    int processWords(Graph<String, ConceptEdge> wordGraph, String farWord, String word, int recursionLevel, Set<String> categoryContext, UNSPSCRecord categoryRecord) {
        if (recursionLevel < MAX_RECUSRSION_LEVEL) {
            recursionLevel++;
            boolean isLowerTreeLevel = false;
            if (recursionLevel == 1)
                isLowerTreeLevel = true;
            //IStrategy processingStrategy = new GeneralStrategy(IS_LOG_ENABLED);
            IStrategy processingStrategy = new ProductStrategy(IS_LOG_ENABLED);
            Set<String> relatedWords = processingStrategy.processWordsInternally(farWord, word, isLowerTreeLevel, categoryContext, categoryRecord);
            //Set<String> relatedWords = processWordsInternally(farWord, word, isLowerTreeLevel, categoryContext, unspscDetails);
            if (relatedWords.size() > 0) {
                for (String relatedWord : relatedWords) {
                    if (!wordGraph.containsVertex(relatedWord)) {
                        wordGraph.addVertex(relatedWord);
                    }
                    addVertexToGraph(wordGraph, relatedWord, word, ConceptEdge._RELATION_TYPE_HYPERNYM, categoryRecord);
                    Utils.doDelay(DELAY);
                    processWords(wordGraph, farWord, Utils.getLabelFromConceptContextName(relatedWord), recursionLevel, categoryContext, categoryRecord);
                }
            }
            else {
                addVertexToGraph(wordGraph, GraphUtils._CONCEPT_ROOT_NODE, word, ConceptEdge._RELATION_TYPE_GENERIC, categoryRecord);
            }
            return recursionLevel;
        } else {
            addVertexToGraph(wordGraph, GraphUtils._CONCEPT_ROOT_NODE, word, ConceptEdge._RELATION_TYPE_GENERIC, categoryRecord);
            return recursionLevel;
        }
    }

    private void addVertexToGraph(Graph<String, ConceptEdge> wordGraph, String source, String target, String edgeType, UNSPSCRecord categoryRecord) {
        if (GraphUtils._CONCEPT_ROOT_NODE.equalsIgnoreCase(source)) {
            if (!wordGraph.containsEdge(GraphUtils._CONCEPT_ROOT_NODE, target)) {
                ConceptEdge newEdge = new ConceptEdge(ConceptEdge._RELATION_TYPE_GENERIC);
                wordGraph.addEdge(GraphUtils._CONCEPT_ROOT_NODE, target, newEdge);
                return;
            }
        }
        if (!wordGraph.containsEdge(source, target)) {
            ConceptEdge newEdge = new ConceptEdge(edgeType, categoryRecord.getUnspsc(), categoryRecord.getUnspscName());
            wordGraph.addEdge(source, target, newEdge);
        }
    }

    /*private Set<String> processWordsInternally(String farWord, String word, boolean isLowerTreeLevel, Set<String> categoryContext, Map<String, String> unspscDetails) {

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
    }*/

}
