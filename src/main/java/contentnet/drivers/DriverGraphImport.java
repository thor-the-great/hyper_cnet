package contentnet.drivers;

import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DriverGraphImport {

    public static void main (String[] args) {
        DriverGraphImport driver = new DriverGraphImport();
        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        GraphUtils.importGraph(wordGraph);
        GraphUtils.displayGraph(wordGraph);
    }
}