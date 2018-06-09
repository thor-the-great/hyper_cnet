package contentnet.drivers;

import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DriverCleanupGraphPerAOData {

    public static void main(String[] args) {
        DriverCleanupGraphPerAOData driver = new DriverCleanupGraphPerAOData();
        driver.doCleanup();
    }

    void doCleanup() {
        String graphFileName = GraphUtils._INWORK_3_CSV_FILE_PATH;
        Set<String> unspscSemanticsWords = getCategoriesSemanticsWords();
        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        //GraphUtils.importGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);
        GraphUtils.importGraph(wordGraph, graphFileName);

        Set<String> vertexSet = wordGraph.vertexSet();
        System.out.println("Graph has vertexes # : " + vertexSet.size());
        Set<String> vertexesToEliminate = new HashSet<>();
        for (String graphWord : vertexSet ) {
            if (graphWord.equalsIgnoreCase(GraphUtils._CONCEPT_ROOT_NODE) || wordGraph.outDegreeOf(graphWord) == 0)
                continue;
            if (graphWord.contains("_")) {
                String[] partsOfComplexWord = graphWord.split("_");
                boolean isAnyPartValid = false;
                for (String partOfComplexWord: partsOfComplexWord) {
                    if (unspscSemanticsWords.contains(partOfComplexWord)) {
                        isAnyPartValid = true;
                        break;
                    }
                }
                //System.out.println(graphWord);
                if (!isAnyPartValid) {
                    vertexesToEliminate.add(graphWord);
                    System.out.println("Term of >1 words to be removed : " + graphWord);
                }
                //check that both words are in the list
                /*int countOfMatches = 0;
                for (String partOfComplexWord: partsOfComplexWord) {
                    if (unspscSemanticsWords.contains(partOfComplexWord)) {
                        countOfMatches++;
                    }
                }
                //System.out.println(graphWord);
                if (countOfMatches < partsOfComplexWord.length) {
                    vertexesToEliminate.add(graphWord);
                    System.out.println(graphWord);
                }*/
            }
            else {
                if (!unspscSemanticsWords.contains(graphWord)) {
                    vertexesToEliminate.add(graphWord);
                }
            }
        }
        System.out.println("This number of vertexes can be simplified : " + vertexesToEliminate.size());

        //do 2 iterations of simplification - after first iteration some ineligible vertexes can became eligible
        doSimplificationIteration(wordGraph, vertexesToEliminate);

        if (vertexesToEliminate.size() > 0 ) {
            doSimplificationIteration(wordGraph, vertexesToEliminate);
        }

        System.out.println("Following number of words are complex cases - many-to-many relations : " + vertexesToEliminate.size());
        for (String vertexToRemove: vertexesToEliminate ) {
            System.out.println(vertexToRemove + "; ");
        }

        //GraphUtils.displayGraph(wordGraph);
        String newFileName = graphFileName.substring(0, graphFileName.lastIndexOf(".")) + "_cleaned" + graphFileName.substring(graphFileName.lastIndexOf("."));
        GraphUtils.exportGraph(wordGraph, newFileName);
    }

    private void doSimplificationIteration(Graph<String, DefaultEdge> wordGraph, Set<String> vertexesToEliminate) {
        for (Iterator<String> it = vertexesToEliminate.iterator(); it.hasNext();) {
            String wordToSimplify = it.next();
            //System.out.println(wordToSimplify);
            //first simplify easiest cases when there is only one outgoing edge. Just remove vertex and join its parents with its child
            Set<DefaultEdge> outgoingEdges =  wordGraph.outgoingEdgesOf(wordToSimplify);
            Set<DefaultEdge> incomingEdges = wordGraph.incomingEdgesOf(wordToSimplify);
            if (outgoingEdges.size() == 1) {
                DefaultEdge edge = outgoingEdges.iterator().next();
                String childVertex = wordGraph.getEdgeTarget(edge);
                for (DefaultEdge incomingEdge : incomingEdges) {
                    String parentVertex = wordGraph.getEdgeSource(incomingEdge);
                    if (!wordGraph.containsEdge(parentVertex, childVertex) && !parentVertex.equalsIgnoreCase(childVertex)) {
                        wordGraph.addEdge(parentVertex, childVertex);
                    }
                }
                wordGraph.removeEdge(edge);
                wordGraph.removeVertex(wordToSimplify);
                it.remove();
                //System.out.println("Removed vertex " + wordToSimplify);
            }
            else {
                //do the other way around - if there is one incoming edge then rewire children to parent
                if (incomingEdges.size() == 1) {
                    DefaultEdge edge = incomingEdges.iterator().next();
                    String parentVertex = wordGraph.getEdgeSource(edge);
                    for (DefaultEdge outgoingEdge : outgoingEdges) {
                        String childVertex = wordGraph.getEdgeTarget(outgoingEdge);
                        if (!wordGraph.containsEdge(parentVertex, childVertex) && !parentVertex.equalsIgnoreCase(childVertex)) {
                            wordGraph.addEdge(parentVertex, childVertex);
                        }
                    }
                    wordGraph.removeEdge(edge);
                    wordGraph.removeVertex(wordToSimplify);
                    it.remove();
                }
                //System.out.println("Complex case : " + wordToSimplify);
            }
        }
    }

    private Set<String> getCategoriesSemanticsWords() {
        Set<String> unspscSemanticsWords = new HashSet<>();
        String fileName = "C:\\work\\ariba\\unspsc_semantics_unique_words.txt";
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            Stream<String> streamSkipped = stream.skip(1);

            Consumer<String> fileLineConsumer = (lineString) -> {
                String wordString = lineString.substring(lineString.indexOf(';') + 1).trim();
                unspscSemanticsWords.add(wordString);
                //System.out.println(wordString);
            };

            streamSkipped.forEach(fileLineConsumer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return unspscSemanticsWords;
    }
}
