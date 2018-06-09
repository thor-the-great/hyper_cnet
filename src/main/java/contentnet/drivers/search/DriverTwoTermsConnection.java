package contentnet.drivers.search;

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
        GraphUtils.importGraph(wordGraph, "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\wordGraph_25_words_5_levels_in_work_cleaned.csv");//GraphUtils._DEFAULT_CSV_FILE_PATH);
        //GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_1_CSV_FILE_PATH);

        String searchWord = "rifle";
        String secondSearchWord = "microphone";
        boolean isConnectionFound = false;

        AllDirectedPaths<String, DefaultEdge> allPathsAlgo = new AllDirectedPaths<>(wordGraph);
        List<GraphPath<String,DefaultEdge>> graphPath = allPathsAlgo.getAllPaths(GraphUtils._CONCEPT_ROOT_NODE, searchWord, true, 10000);
        for (GraphPath<String, DefaultEdge> onePath: graphPath) {
            //System.out.println(onePath);
            List<String> onePathVertexList = onePath.getVertexList();
            //iterate over vertexes excluding root node and word itself
            //check that any node has connection to the second word
            for (int i = 1; i < onePathVertexList.size() -1; i++) {
                String pathVertex = onePathVertexList.get(i);
                List<GraphPath<String,DefaultEdge>> possiblePath = allPathsAlgo.getAllPaths(pathVertex, secondSearchWord, true, 1000);
                if (possiblePath != null && possiblePath.size() > 0) {
                    System.out.println("Words are connected : '" + searchWord +"' == '" +secondSearchWord + "'");
                    for (GraphPath<String,DefaultEdge> path: possiblePath) {
                        System.out.println(path);
                    }
                    //isConnectionFound = true;
                    break;
                }
            }
            if (isConnectionFound)
                break;
        }
    }
}
