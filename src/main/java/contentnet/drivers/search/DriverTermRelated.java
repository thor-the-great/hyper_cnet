package contentnet.drivers.search;

import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.Set;

public class DriverTermRelated {

    public static void  main(String[] args) {
        String wordToSearch = "sleeve";
        int depth = 1;
        DriverTermRelated driver = new DriverTermRelated();
        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        //GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_1_CSV_FILE_PATH);
        GraphUtils.importGraph(wordGraph, "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\wordGraph_25_words_5_levels_in_work_cleaned.csv");
        //GraphUtils.importGraph(wordGraph, "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\wordGraph.csv");

        driver.findRelated(wordGraph, wordToSearch, depth);
    }

    void findRelated(Graph<String, DefaultEdge> wordGraph, String wordToSearch, int depth) {
        if (!wordGraph.containsVertex(wordToSearch)) {
            System.out.println("Word is not in graph");
            return;
        }
        Set<DefaultEdge> incomingEdges = wordGraph.incomingEdgesOf(wordToSearch);
        Set<String> parentVertexes = new HashSet<>();
        Set<String> relatedVertexes = new HashSet<>();
        Set<String> visitedVertexes = new HashSet<>();
        visitedVertexes.add(GraphUtils._CONCEPT_ROOT_NODE);
        visitedVertexes.add(wordToSearch);
        for (DefaultEdge edge: incomingEdges) {
            String parentVertex = wordGraph.getEdgeSource(edge);
            if (visitedVertexes.contains(parentVertex))
                continue;
            parentVertexes.add(parentVertex);
            visitedVertexes.add(parentVertex);
        }

        for (String parentVertex: parentVertexes) {
            handleParentVertexesRec(wordGraph, wordToSearch, parentVertex, relatedVertexes, visitedVertexes);
        }
    }

    private void handleParentVertexesRec(Graph<String, DefaultEdge> wordGraph, String wordToSearch, String parentVertex, Set<String> relatedVertexes, Set<String> visitedVertexes) {
        //for (String parentVertex: parentVertexes) {
            Set<DefaultEdge> outgoingParentEdges = wordGraph.outgoingEdgesOf(parentVertex);
            for (DefaultEdge outgoingParentEdge: outgoingParentEdges) {
                String parentChildVertex = wordGraph.getEdgeTarget(outgoingParentEdge);
                if (parentChildVertex.equalsIgnoreCase(wordToSearch) || visitedVertexes.contains(parentChildVertex))
                    continue;
                if (wordGraph.outDegreeOf(parentChildVertex) == 0) {
                    relatedVertexes.add(parentChildVertex);
                    visitedVertexes.add(parentChildVertex);
                    System.out.println(parentChildVertex);
                } else {
                    visitedVertexes.add(parentChildVertex);
                    handleParentVertexesRec(wordGraph, wordToSearch, parentChildVertex, relatedVertexes, visitedVertexes);
                }
            }
        //}
    }
}
