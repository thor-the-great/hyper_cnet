package contentnet.drivers;

import contentnet.graph.ConceptEdge;
import contentnet.graph.ConceptGraphCSVImporterSimple;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.CSVFormat;

public class DriverGraphImport {

    public static void main (String[] args) {
        DriverGraphImport driver = new DriverGraphImport();
        Graph<String, ConceptEdge> wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
        //GraphUtils.importGraph(wordGraph,GraphUtils._INWORK_1_CSV_FILE_PATH);
        //GraphUtils.importGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);
        //ConceptGraphCSVImporterSimple graphImporter = new ConceptGraphCSVImporterSimple();
        //graphImporter.importGraph(wordGraph, "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\wordGraph_43211708.csv");
        //graphImporter.importGraph(wordGraph, "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\wordGraph_43211706 - Product_Strategy.csv", CSVFormat.ADJACENCY_LIST);
        //graphImporter.importGraph(wordGraph, "C:\\work\\ariba\\export_test_expr.csv", CSVFormat.MATRIX);
        GraphUtils.importGraphMatrix(wordGraph, "C:\\work\\ariba\\export_test_expr.csv");
        //graphImporter.importGraph(, CSVFormat.MATRIX);
        GraphUtils.displayGraph(wordGraph);
        GraphUtils.exportGraph("C:\\work\\ariba\\export_test_expr2.csv", wordGraph, CSVFormat.MATRIX);
    }
}
