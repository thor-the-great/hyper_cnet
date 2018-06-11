package contentnet.drivers;

import contentnet.graph.ConceptEdge;
import contentnet.graph.ConceptGraphCSVImporterSimple;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class DriverGraphImport {

    public static void main (String[] args) {
        DriverGraphImport driver = new DriverGraphImport();
        Graph<String, ConceptEdge> wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
        //GraphUtils.importGraph(wordGraph,GraphUtils._INWORK_1_CSV_FILE_PATH);
        //GraphUtils.importGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);
        ConceptGraphCSVImporterSimple graphImporter = new ConceptGraphCSVImporterSimple();
        graphImporter.importGraph(wordGraph, "C:\\work\\ariba\\export_test.csv");
        GraphUtils.displayGraph(wordGraph);
        GraphUtils.exportGraph("C:\\work\\ariba\\export_test_expr.csv", wordGraph);
    }
}
