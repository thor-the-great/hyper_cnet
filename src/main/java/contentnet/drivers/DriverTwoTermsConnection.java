package contentnet.drivers;

import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

import java.util.List;

/**
 * Test finding connection between two words
 */
public class DriverTwoTermsConnection {

    public static void main(String[] args) {
        DriverTwoTermsConnection driver = new DriverTwoTermsConnection();
        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        GraphUtils.importGraph(wordGraph, "C:\\work\\ariba\\wordGraph - 20 words fixed.csv");//GraphUtils._DEFAULT_CSV_FILE_PATH);

        String searchWord = "cable";
        AllDirectedPaths<String, DefaultEdge> allPathsAlgo = new AllDirectedPaths<>(wordGraph);
        List<GraphPath<String,DefaultEdge>> graphPath = allPathsAlgo.getAllPaths(GraphUtils._CONCEPT_ROOT_NODE, searchWord, false, Integer.MAX_VALUE);
        for (GraphPath<String, DefaultEdge> onePath: graphPath) {

        }
    }
}
