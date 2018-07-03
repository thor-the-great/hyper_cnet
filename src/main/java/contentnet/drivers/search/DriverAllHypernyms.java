package contentnet.drivers.search;

import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DriverAllHypernyms {
    public static void main (String[] args) {
        DriverAllHypernyms drive = new DriverAllHypernyms();
        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        //GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_1_CSV_FILE_PATH);
        drive.getAllHypernyms("case", wordGraph);
    }

    void getAllHypernyms(String searchWord, Graph<String, DefaultEdge> graph) {
        AllDirectedPaths<String, DefaultEdge> allPathsAlgo = new AllDirectedPaths<>(graph);
        List<GraphPath<String, DefaultEdge>> possiblePaths = allPathsAlgo.getAllPaths(GraphUtils._CONCEPT_ROOT_NODE, searchWord, true, Integer.MAX_VALUE);
        Set<String> hypernyms = new HashSet<>();
        for (GraphPath<String, DefaultEdge> path: possiblePaths) {
            //System.out.println(path);
            hypernyms.addAll(path.getVertexList());
        }

        for (String hypernym: hypernyms) {
            System.out.println(hypernym);
        }
    }
}
