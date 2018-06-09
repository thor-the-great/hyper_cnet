package contentnet.graph;

import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import contentnet.GlobalProperties;
import org.jgrapht.Graph;
import org.jgrapht.demo.LabeledEdges;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class GraphUtils {

    public static final String _DEFAULT_CSV_FILE_PATH = GlobalProperties.getSetting("DEFAULT_CSV_FILE_PATH");
    public static final String _INWORK_1_CSV_FILE_PATH = GlobalProperties.getSetting("INWORK_1_CSV_FILE_PATH");
    public static final String _INWORK_2_CSV_FILE_PATH = GlobalProperties.getSetting("INWORK_2_CSV_FILE_PATH");
    public static final String _INWORK_3_CSV_FILE_PATH = GlobalProperties.getSetting("INWORK_3_CSV_FILE_PATH");


    public static final String _CONCEPT_ROOT_NODE = "CONCEPT_ROOT_NODE";

    /*public static void displayGraph(Graph<String, DefaultEdge> wordGraph) {
        JFrame frame = new JFrame("Concept net word graph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JGraphXAdapter<String, DefaultEdge> graphAdapter = new JGraphXAdapter<>(wordGraph);

        //mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        frame.add(new mxGraphComponent(graphAdapter));

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }*/

    public static void displayGraph(Graph<String, ConceptEdge> wordGraph) {
        displayGraph(wordGraph, null);
    }

    public static void displayGraph(Graph<String, ConceptEdge> wordGraph, String title) {
        JFrame frame = new JFrame(title == null ? "Concept net word graph" : title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JGraphXAdapter<String, ConceptEdge> graphAdapter = new JGraphXAdapter<>(wordGraph);

        //mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        frame.add(new mxGraphComponent(graphAdapter));

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    public static void exportGraph(Graph<String, DefaultEdge> graph, String fileToExport) {
        File exportFile = new File(fileToExport);
        //CSVExporter<String, DefaultEdge> exporter = new CSVExporter();
        CSVExporter<String, DefaultEdge> exporter = new CSVExporter(new StringComponentNameProvider<String>(), CSVFormat.ADJACENCY_LIST, ',');
        try {
            exporter.exportGraph(graph, exportFile);
        } catch (ExportException e) {
            e.printStackTrace();
        }
    }

    public static void exportGraph(String fileToExport, Graph<String, ConceptEdge> graph) {
        File exportFile = new File(fileToExport);
        //CSVExporter<String, DefaultEdge> exporter = new CSVExporter();
        //CSVExporter<String, ConceptEdge> exporter = new CSVExporter(new StringComponentNameProvider<String>(), CSVFormat.ADJACENCY_LIST, ',');
        ConceptGraphCSVExport exporter = new ConceptGraphCSVExport();
        try {
            exporter.exportGraph(graph, exportFile);
        } catch (ExportException e) {
            e.printStackTrace();
        }
    }

    public static Graph<String, DefaultEdge> importGraph(Graph<String, DefaultEdge> graph, String filePath) {
        File graphFile = new File(filePath);
        CSVImporter<String, DefaultEdge> graphImporter = new CSVImporter(
                new VertexProvider() {
                    @Override
                    public Object buildVertex(String s, Map map) {
                        return s;
                    }
                },
                new EdgeProvider() {
                    @Override
                    public Object buildEdge(Object o, Object v1, String s, Map map) {
                        DefaultEdge edge = new DefaultEdge();
                        return edge;
                        //return null;
                    }
                });
        try {
            graphImporter.importGraph(graph, graphFile);
        } catch (ImportException e) {
            e.printStackTrace();
        }
        return graph;
    }

    public static void printGraph(Graph<String, DefaultEdge> wordGraph) {
        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator<>(wordGraph);
        while (iterator.hasNext()) {
            String vertex = iterator.next();
            Set<DefaultEdge> edges = wordGraph.outgoingEdgesOf(vertex);
            for (DefaultEdge edge: edges) {
                System.out.println(wordGraph.getEdgeSource(edge) + "->" + wordGraph.getEdgeTarget(edge));
            }
        }
    }

    public static void printGraphNew(Graph<String, ConceptEdge> wordGraph) {
        GraphIterator<String, ConceptEdge> iterator = new DepthFirstIterator<>(wordGraph);
        while (iterator.hasNext()) {
            String vertex = iterator.next();
            Set<ConceptEdge> edges = wordGraph.outgoingEdgesOf(vertex);
            for (ConceptEdge edge: edges) {
                System.out.println(wordGraph.getEdgeSource(edge) + "->" + wordGraph.getEdgeTarget(edge));
            }
        }
    }
}
