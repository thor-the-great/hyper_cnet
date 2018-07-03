package contentnet.drivers;

import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphAPI;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.CSVFormat;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;

public class DriverSemanticOptimization {

    public static void main(String[] args) {
        DriverSemanticOptimization driver = new DriverSemanticOptimization();
        driver.doWork();
        //driver.collectStatistics();
    }

    public void doWork() {
        Graph<String, ConceptEdge> wordGraph = new DirectedMultigraph<>(ConceptEdge.class);
        //String fileName = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\wordGraph_43211604.csv";
        String fileName = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\wordGraph_43211804.csv";

        String exportFileName = fileName + ".proc";
        GraphUtils.importGraphMatrix(wordGraph, fileName);
        GraphUtils.displayGraph(wordGraph);

        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator(wordGraph, GraphUtils._CONCEPT_ROOT_NODE);
        //boolean categoryCaptured = false;
        String sourceVertex = GraphUtils._CONCEPT_ROOT_NODE;
        String unspscCategory = "", unspscCategoryName = "";
        while (iterator.hasNext()) {
            String nextVertex = iterator.next();
            Set<ConceptEdge> edges = wordGraph.getAllEdges(sourceVertex, nextVertex);
            for (ConceptEdge edge: edges) {
                if(ConceptEdge._RELATION_TYPE_SYNONYM.equals(edge.getRelationType()) || ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edge.getRelationType())) {
                    unspscCategory = (String)edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY);
                    unspscCategoryName = (String)edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME);
                    break;
                }
            }
            sourceVertex = nextVertex;
        }
        System.out.println("Graph imported");

        List<String> stopWords = GraphUtils.getStopWords();
        List<String> relaxedStopWords = GraphUtils.getRelaxedStopWords();

        iterator = new DepthFirstIterator(wordGraph, GraphUtils._CONCEPT_ROOT_NODE);
        while (iterator.hasNext()) {
            String nextVertex = "";
            try {
                nextVertex = iterator.next();
            } catch (IllegalArgumentException iae) {
                //iae.printStackTrace();
                continue;
            }
            if (!wordGraph.containsVertex(nextVertex))
                continue;

            //deal with stop words
            dealWithStopWords(wordGraph, stopWords, nextVertex);

            if (!wordGraph.containsVertex(nextVertex))
                continue;

            //dealWithStopWords(wordGraph, relaxedStopWords, nextVertex);
            if (!wordGraph.containsVertex(nextVertex))
                continue;

            //check that this is not the first level node and it does have more than one word in it
            if (wordGraph.outDegreeOf(nextVertex) == 0)
                continue;
            if (!nextVertex.contains("_"))
                continue;
            if (GraphUtils._CONCEPT_ROOT_NODE.equals(nextVertex))
                continue;

            System.out.println("Found potential term : " + nextVertex);
            Set<String> wordForms = ConceptnetAPI.getInstance().getWordForms(nextVertex);
            String[] partsOfTerm = nextVertex.split("_");
            String allWordsJoined = "";
            for (String partOfTerm : partsOfTerm) {
                allWordsJoined += partOfTerm;
            }
            boolean isOptimized = false;
            for (String wordForm: wordForms) {
                if (allWordsJoined.equalsIgnoreCase(wordForm)) {
                    System.out.println("Found possible transformation 1 : " + nextVertex + " -> " + wordForm);
                    replaceVertex(wordGraph, nextVertex, wordForm);
                    isOptimized = true;
                    break;
                }
            }
            if (isOptimized)
                continue;

            Set<String> isAWords =
                    ResultProcessor.getInstance().extractEdgeEnds(
                            ConceptnetAPI.getInstance().getIsA(nextVertex), nextVertex);
            if (isAWords.size() > 0 ) {
                for (String partOfTerm : partsOfTerm) {
                    for (String nextIsAWord : isAWords ) {
                        if (nextIsAWord.equalsIgnoreCase(partOfTerm)) {
                            System.out.println("Found possible transformation 2 : " + nextVertex + " -> " + nextIsAWord);
                            replaceVertex(wordGraph, nextVertex, nextIsAWord);
                            isOptimized = true;
                            break;
                        }
                    }
                }
            }

            if (isOptimized)
                continue;

            Map<String, List<String>> semanticMappings = GraphUtils.getSemanticMapping();
            if (semanticMappings.containsKey(nextVertex)) {
                List<String> mappedWords = semanticMappings.get(nextVertex);
                if (mappedWords.size() >= 1) {
                    String replaceWord = mappedWords.get(0);
                    replaceVertex(wordGraph, nextVertex, replaceWord);
                    if (mappedWords.size() >= 2) {
                        for (int i = 1; i < mappedWords.size(); i++) {
                            String mappedWord = mappedWords.get(i);
                            if (!wordGraph.containsVertex(mappedWord))
                                wordGraph.addVertex(mappedWord);
                            GraphAPI.addEdgeToGraph(wordGraph, replaceWord, mappedWord, ConceptEdge._RELATION_TYPE_SYNONYM, unspscCategory, unspscCategoryName);
                        }
                    }
                }
                isOptimized = true;
            }

            if (isOptimized)
                continue;
        }//while for each vertex

        //this is advanced optimization/enrichment
        Map<String, GraphUtils.SemanticCategoryEnrichmentObject> categories = GraphUtils.getEnrichedCategories();
        //Map<String, String> hypernyms = categories.get(0);
        //Map<String, String> synonyms = categories.get(1);

        //doEnrichmentOptimization(wordGraph, unspscCategory, unspscCategoryName, hypernyms, ConceptEdge._RELATION_TYPE_HYPERNYM);
        //doEnrichmentOptimization(wordGraph, unspscCategory, unspscCategoryName, synonyms, ConceptEdge._RELATION_TYPE_SYNONYM);
        doEnrichmentOptimization(wordGraph, unspscCategory, unspscCategoryName, categories);

        sanitizeGraph(wordGraph);

        iterator = new DepthFirstIterator(wordGraph, GraphUtils._CONCEPT_ROOT_NODE);
        while (iterator.hasNext()) {
            String nextVertex = "";
            try {
                nextVertex = iterator.next();
            } catch (IllegalArgumentException iae) {
                //iae.printStackTrace();
                continue;
            }
            if (!wordGraph.containsVertex(nextVertex))
                continue;

            //deal with stop words
            dealWithStopWords(wordGraph, stopWords, nextVertex);

            if (!wordGraph.containsVertex(nextVertex))
                continue;
        }

        sanitizeGraph(wordGraph);

        GraphUtils.displayGraph(wordGraph);
        GraphUtils.exportGraph(exportFileName, wordGraph, CSVFormat.MATRIX);
    }

    private boolean dealWithStopWords(Graph<String, ConceptEdge> wordGraph, List<String> stopWords, String nextVertex) {
        if(stopWords.contains(nextVertex) && wordGraph.containsVertex(nextVertex)) {
            System.out.println("Found stop word " + nextVertex);
            //go up the tree and delete all direct parents (along with synonyms)
            Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(nextVertex);
            Set<ConceptEdge> outgoingEdges = wordGraph.outgoingEdgesOf(nextVertex);
            if (incomingEdges.size() == 0 && outgoingEdges.size() ==0 ) {
                wordGraph.removeVertex(nextVertex);
                return true;
            }
            boolean isAllSynonyms = true;
            for (ConceptEdge incomingEdge: incomingEdges) {
                if (!ConceptEdge._RELATION_TYPE_SYNONYM.equals(incomingEdge.getRelationType())) {
                    isAllSynonyms = false;
                    break;
                }
            }
            if (isAllSynonyms) {
                for (ConceptEdge outEdge : outgoingEdges ) {
                    if (!ConceptEdge._RELATION_TYPE_SYNONYM.equals(outEdge.getRelationType())) {
                        isAllSynonyms = false;
                        break;
                    }
                }
            }

            if (isAllSynonyms) {
                wordGraph.removeVertex(nextVertex);
                System.out.println("Removed stop word " + nextVertex);
                return true;
            }

            Stack<String> sourceVertexOfIncoming = new Stack<>();
            sourceVertexOfIncoming.add(nextVertex);
            while(!sourceVertexOfIncoming.empty()) {
                String inWorkVertex = sourceVertexOfIncoming.pop();
                if (!GraphUtils._CONCEPT_ROOT_NODE.equals(inWorkVertex) && wordGraph.containsVertex(inWorkVertex)) {
                    Set<ConceptEdge> incomingEdgesInWork = wordGraph.incomingEdgesOf(inWorkVertex);
                    if (incomingEdgesInWork != null && incomingEdgesInWork.size() > 0 ){
                        for (ConceptEdge edge : incomingEdgesInWork) {
                            sourceVertexOfIncoming.push(edge.getSource());
                        }
                    }
                    wordGraph.removeVertex(inWorkVertex);
                }
            }

            //first delete all subtrees below completely
            /*GraphIterator<String, DefaultEdge> goingDownDFSIterator = new DepthFirstIterator(wordGraph, nextVertex);
            while(goingDownDFSIterator.hasNext()) {
                String nextVertexDown = goingDownDFSIterator.next();
                if (!nextVertexDown.equals(nextVertex)) {
                    goingDownDFSIterator.remove();
                }
            }*/
        }
        return false;
    }

    /*private void doEnrichmentOptimization(Graph<String, ConceptEdge> wordGraph, String unspscCategory, String unspscCategoryName, Map<String, String> hypernymsOrSynonyms, String relationTypeToCheck) {
        Set<String> vertexSetIteration = new HashSet<>(wordGraph.vertexSet());
        for (String vertex: vertexSetIteration) {
            if (hypernymsOrSynonyms.containsKey(vertex) && wordGraph.containsVertex(vertex)) {
                Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(vertex);
                for (ConceptEdge incomingEdge : incomingEdges ) {
                    if (relationTypeToCheck.equals(incomingEdge.getRelationType())) {
                        //this will clean all the nodes from this one (exclusive) to the root
                        removeVertexTree(wordGraph, incomingEdge.getSource());
                        //this is add segment from the categories to the graph
                        String itVertex = vertex;
                        while(itVertex != null) {
                            String hypernymVertex = hypernymsOrSynonyms.get(itVertex);
                            if (hypernymVertex != null) {
                                if (!wordGraph.containsVertex(hypernymVertex))
                                    wordGraph.addVertex(hypernymVertex);
                                GraphAPI.addEdgeToGraph(wordGraph, hypernymVertex, itVertex, relationTypeToCheck, unspscCategory, unspscCategoryName);
                            }
                            else {
                                GraphAPI.addEdgeToGraph(wordGraph, GraphUtils._CONCEPT_ROOT_NODE, itVertex, ConceptEdge._RELATION_TYPE_GENERIC, null, null);
                            }
                            itVertex = hypernymVertex;
                        }
                    }
                }
            }
        }
    }*/
    private void doEnrichmentOptimization(Graph<String, ConceptEdge> wordGraph, String unspscCategory, String unspscCategoryName, Map<String, GraphUtils.SemanticCategoryEnrichmentObject> categories) {
        Set<String> vertexSetIteration = new HashSet<>(wordGraph.vertexSet());
        for (String vertex: vertexSetIteration) {
            if (categories.containsKey(vertex) && wordGraph.containsVertex(vertex)) {
                System.out.println("---- Start processing for word " + vertex);
                Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(vertex);
                Set<ConceptEdge> incomingEdgesCopy = new HashSet<>(incomingEdges);
                GraphUtils.SemanticCategoryEnrichmentObject semanticCategoryObj = categories.get(vertex);
                String relationTypeToCheck = "";
                if (GraphUtils.SemanticCategoryEnrichmentObject.HYPERNYM_RELATION_TYPE.equals(semanticCategoryObj.getType()))
                    relationTypeToCheck = ConceptEdge._RELATION_TYPE_HYPERNYM;
                else if (GraphUtils.SemanticCategoryEnrichmentObject.SYNONYM_RELATION_TYPE.equals(semanticCategoryObj.getType()))
                    relationTypeToCheck = ConceptEdge._RELATION_TYPE_SYNONYM;
                for ( Iterator<ConceptEdge> it = incomingEdgesCopy.iterator(); it.hasNext(); ) {
                    ConceptEdge incomingEdge = it.next();
                    if (relationTypeToCheck.equals(incomingEdge.getRelationType())) {
                        //this will clean all the nodes from this one (exclusive) to the root
                        if (!semanticCategoryObj.isKeepExistingEdges()) {
                            removeVertexTree(wordGraph, incomingEdge.getSource());
                        }
                        //this is add segment from the categories to the graph
                        String itVertex = vertex;
                        while(itVertex != null) {
                            if (categories.get(itVertex) != null) {
                                String hypernymVertex = categories.get(itVertex).getTarget();
                                if (!wordGraph.containsVertex(hypernymVertex)) {
                                    System.out.println("Added vertex : " + hypernymVertex);
                                    wordGraph.addVertex(hypernymVertex);
                                }
                                GraphAPI.addEdgeToGraph(wordGraph, hypernymVertex, itVertex, relationTypeToCheck, unspscCategory, unspscCategoryName);
                                System.out.println("Added edge " + hypernymVertex + " -> " + itVertex);
                                itVertex = hypernymVertex;
                            }
                            else {
                                GraphAPI.addEdgeToGraph(wordGraph, GraphUtils._CONCEPT_ROOT_NODE, itVertex, ConceptEdge._RELATION_TYPE_GENERIC, null, null);
                                System.out.println("Added edge ROOT -> " + itVertex);
                                itVertex = null;
                            }
                        }
                    }
                }
            }
        }
    }


    private void removeVertexTree(Graph<String, ConceptEdge> wordGraph, String vertex) {
        if (GraphUtils._CONCEPT_ROOT_NODE.equals(vertex))
            return;
        Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(vertex);

        Set<String> sourceVertexes = new HashSet<>();

        for (ConceptEdge edge : incomingEdges) {
            sourceVertexes.add(edge.getSource());
        }

        for (String sourceVertex : sourceVertexes) {
            wordGraph.removeEdge(sourceVertex, vertex);
        }
        if (wordGraph.inDegreeOf(vertex) == 0) {
            wordGraph.removeVertex(vertex);
            System.out.println("Removed vertex : " + vertex);
        }
        for (String sourceVertex : sourceVertexes) {
            removeVertexTree(wordGraph, sourceVertex);
        }
        System.out.println("Processed vertex : " + vertex);
    }

    public static void replaceVertex(Graph<String, ConceptEdge> graph, String vertex, String replace) {
        if (!graph.containsVertex(replace)) {
            graph.addVertex(replace);
        }
        for (ConceptEdge edge : graph.outgoingEdgesOf(vertex)) {
            //Set<ConceptEdge> allEdgesBetweenTwo = graph.getAllEdges(vertex, edge.getEdgeDestination());
            Set<ConceptEdge> allEdgesBetweenTwo = graph.getAllEdges(replace, edge.getEdgeDestination());
            boolean isThereIsAnEdge = false;
            if (allEdgesBetweenTwo != null) {
                for (ConceptEdge edgeBetweenTwo : allEdgesBetweenTwo) {
                    if (edgeBetweenTwo.getRelationType().equals(edge.getRelationType())) {
                        isThereIsAnEdge = true;
                        break;
                    }
                }
            }
            if(!isThereIsAnEdge && !replace.equals(edge.getTarget())) {
                ConceptEdge newEdge = (ConceptEdge) edge.clone();
                //newEdge.setEdgeSourceDestination(vertex, edge.getEdgeDestination());
                newEdge.setEdgeSourceDestination(replace, edge.getEdgeDestination());
                graph.addEdge(replace, edge.getTarget(), newEdge);
            }
        }
        for (ConceptEdge edge : graph.incomingEdgesOf(vertex)) {
            //Set<ConceptEdge> allEdgesBetweenTwo = graph.getAllEdges(edge.getEdgeSource(), vertex);
            Set<ConceptEdge> allEdgesBetweenTwo = graph.getAllEdges(edge.getEdgeSource(), replace);
            boolean isThereIsAnEdge = false;
            if (allEdgesBetweenTwo != null) {
                for (ConceptEdge edgeBetweenTwo : allEdgesBetweenTwo) {
                    if (edgeBetweenTwo.getRelationType().equals(edge.getRelationType())) {
                        isThereIsAnEdge = true;
                        break;
                    }
                }
            }
            if(!isThereIsAnEdge && !replace.equals(edge.getSource())) {
                ConceptEdge newEdge = (ConceptEdge) edge.clone();
                //newEdge.setEdgeSourceDestination(edge.getEdgeSource(), vertex);
                newEdge.setEdgeSourceDestination(edge.getEdgeSource(), replace);
                graph.addEdge(edge.getSource(), replace, newEdge);
            }
        }
        graph.removeVertex(vertex);
    }

    private void sanitizeGraph(Graph<String, ConceptEdge> graph) {
        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator(graph, GraphUtils._CONCEPT_ROOT_NODE);
        while (iterator.hasNext()) {
            String nextVertex = iterator.next();
            Set<String> directChildren = new HashSet<>();
            Set<ConceptEdge> edges = graph.outgoingEdgesOf(nextVertex);
            for (ConceptEdge edge : edges) {
                directChildren.add(edge.getTarget());
            }
            for ( String directChildVertex : directChildren ) {
                Set<ConceptEdge> directEdges = graph.getAllEdges(nextVertex, directChildVertex);
                int genCount = 0;
                int hyperCount = 0;
                int synonymCount = 0;
                for (Iterator<ConceptEdge> it = directEdges.iterator(); it.hasNext();) {
                    ConceptEdge edge = it.next();
                    if (ConceptEdge._RELATION_TYPE_GENERIC.equals(edge.getRelationType())) {
                        if (genCount == 0)
                            genCount++;
                        else {
                            it.remove();
                            graph.removeEdge(edge);
                        }
                    } else if (ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edge.getRelationType())) {
                        if (hyperCount == 0)
                            hyperCount++;
                        else {
                            it.remove();
                            graph.removeEdge(edge);
                        }
                    } else if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edge.getRelationType())) {
                        if (synonymCount == 0)
                            synonymCount++;
                        else {
                            it.remove();
                            graph.removeEdge(edge);
                        }
                    }
                }
            }

        }
    }

    public void collectStatistics() {
        String folderName = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data";
        try (Stream<Path> paths = walk(Paths.get(folderName), 1)) {
            paths.filter(Files::isRegularFile)
                    //.filter(filePath -> filePath.startsWith("hwByCategory_") && filePath.endsWith(".txt"))
                    .filter(filePath ->filePath.getFileName().toString().endsWith(".txt") && filePath.getFileName().toString().startsWith("hwByCategory_"))
                    .forEach(filePath -> {
                        //System.out.println("Processing file " + filePath);
                        BufferedReader br = null;
                        String categoryCode = "", categoryCodeDescription = "";
                        String[] wordsArray = null;
                        try {
                            br = new BufferedReader(new FileReader(filePath.toString()));
                            String categoryValues = null;
                            categoryValues = br.readLine().split(":")[1];
                            int firstSeparatorIndex = categoryValues.indexOf(",");
                            categoryCode = categoryValues.substring(0, firstSeparatorIndex);
                            categoryCodeDescription = categoryValues.substring(firstSeparatorIndex + 1);
                            String[] values = br.readLine().split(":");
                            if (values.length < 2)
                                wordsArray = new String[0];
                            else {
                                String wordsLine = values[1];
                                wordsArray = wordsLine.split(",");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println(categoryCode + "," + categoryCodeDescription +"," + wordsArray.length);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
