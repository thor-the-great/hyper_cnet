package contentnet.graph;

import com.mxgraph.layout.*;
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraphView;
import contentnet.GlobalProperties;
import contentnet.category.UNSPSCRecord;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.event.VertexSetListener;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.io.*;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.util.*;

public class GraphUtils {

    public static final String _DEFAULT_CSV_FILE_PATH = GlobalProperties.getSetting("DEFAULT_CSV_FILE_PATH");
    public static final String _INWORK_1_CSV_FILE_PATH = GlobalProperties.getSetting("INWORK_1_CSV_FILE_PATH");
    public static final String _INWORK_2_CSV_FILE_PATH = GlobalProperties.getSetting("INWORK_2_CSV_FILE_PATH");
    public static final String _INWORK_3_CSV_FILE_PATH = GlobalProperties.getSetting("INWORK_3_CSV_FILE_PATH");

    static List<String> _stopWords = null;


    public static final String _CONCEPT_ROOT_NODE = "CONCEPT_ROOT_NODE";

    public static void displayGraph(Graph<String, ConceptEdge> wordGraph) {
        displayGraph(wordGraph, null);
    }

    public static void displayGraph(Graph<String, ConceptEdge> wordGraph, String title) {
        JFrame frame = new JFrame(title == null ? "Concept net word graph" : title);
        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ListenableGraph<String, ConceptEdge> lGraph = new DefaultListenableGraph<>(wordGraph);
        lGraph.addVertexSetListener(new VertexSetListener<String>() {
            @Override
            public void vertexAdded(GraphVertexChangeEvent<String> e) {
                System.out.println("Vertex added");
            }

            @Override
            public void vertexRemoved(GraphVertexChangeEvent<String> e) {
                System.out.println("Vertex removed");
            }
        });

        //JGraphXAdapter<String, ConceptEdge> graphAdapter = new JGraphXAdapter<>(wordGraph);
        JGraphXAdapter<String, ConceptEdge> graphAdapter = new JGraphXAdapter<>(lGraph);
        graphAdapter.setCellsResizable(true);

        //mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());


        frame.add(new mxGraphComponent(graphAdapter));

        mxGraphView view =graphAdapter.getView();

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);

        view.setScale(2.0f);

    }

    public static void exportGraph(String fileToExport, Graph<String, ConceptEdge> graph, CSVFormat format) {
        File exportFile = new File(fileToExport);
        ConceptGraphCSVExport exporter = new ConceptGraphCSVExport(format);
        try {
            exporter.exportGraph(graph, exportFile);
        } catch (ExportException e) {
            e.printStackTrace();
        }
    }

    public static void exportGraph(String fileToExport, Graph<String, ConceptEdge> graph) {
        exportGraph(fileToExport, graph, CSVFormat.ADJACENCY_LIST);
    }

    public static Graph<String, ConceptEdge> importGraphMatrix(Graph<String, ConceptEdge> graph, String filePath) {
        ConceptGraphCSVImporterSimple graphImporter = new ConceptGraphCSVImporterSimple();
        graphImporter.importGraph(graph, filePath, CSVFormat.MATRIX);
        return graph;
    }

    public static Graph<String, ConceptEdge> importGraphAdjList(Graph<String, ConceptEdge> graph, String filePath) {
        ConceptGraphCSVImporterSimple graphImporter = new ConceptGraphCSVImporterSimple();
        graphImporter.importGraph(graph, filePath, CSVFormat.ADJACENCY_LIST);
        return graph;
    }

    public static String[] getGraphCategoryInfo(Graph<String, ConceptEdge> graph) {
        String[] result = new String[]{"", ""};
        for (ConceptEdge edge: graph.edgeSet()) {
            if (!ConceptEdge._RELATION_TYPE_GENERIC.equals(edge.getRelationType())) {
                result[0] = (String) edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY);
                result[1] = (String) edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME);
                break;
            }
        }
        return result;
    }
/*
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
    */

    public static Map<String, List<String>> getSemanticMapping() {
        String mappingFilename = GlobalProperties.getSetting("data.semnatics.semantic_mapping");
        Map<String, List<String>> result = new HashMap<>();
        try {
            Scanner fileScanner = new Scanner(new FileInputStream(mappingFilename));
            while (fileScanner.hasNext()) {
                String nextLine = fileScanner.nextLine();
                String[] values = nextLine.split(",");
                List<String> valueList = result.get(values[0]);
                if (valueList == null) {
                    valueList = new ArrayList<>();
                    result.put(values[0], valueList);
                }
                valueList.add(values[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Map<String, SemanticCategoryEnrichmentObject> getEnrichedCategories() {
        String mappingFilename = GlobalProperties.getSetting("data.semnatics.categories");

        Map<String, SemanticCategoryEnrichmentObject> result = new HashMap<>();
        try {
            Scanner fileScanner = new Scanner(new FileInputStream(mappingFilename));
            while (fileScanner.hasNext()) {
                SemanticCategoryEnrichmentObject newSemObject = new SemanticCategoryEnrichmentObject();
                String nextLine = fileScanner.nextLine();
                String[] values = nextLine.split(",");
                newSemObject.source = values[0];
                newSemObject.target = values[2];
                newSemObject.type = values[1];
                newSemObject.isKeepExistingEdges = Boolean.parseBoolean(values[3]);
                result.put(newSemObject.source, newSemObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static List<String> getListOfValues(String stopWordsFile) {
        List<String> stopWordsList = new ArrayList<>();

        try {
            Scanner fScanner = new Scanner(new FileInputStream(stopWordsFile));
            while (fScanner.hasNext()) {
                String nextWord = fScanner.nextLine();
                stopWordsList.add(nextWord);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return stopWordsList;
    }

    public static List<String> getStopWords() {
        if (_stopWords == null ) {
            List<String> stopWordsDynamicList = getListOfValues(GlobalProperties.getSetting("data.semnatics.stop_words_dynamic"));
            List<String> stopWordsStaticList = getListOfValues(GlobalProperties.getSetting("data.semnatics.stop_words_static"));
            if (stopWordsStaticList != null)
                stopWordsDynamicList.addAll(stopWordsStaticList);
            _stopWords = stopWordsDynamicList;
        }
        return _stopWords;
    }

    public static List<String> getRelaxedStopWords() {
        List<String> stopWordsList = getListOfValues(GlobalProperties.getSetting("data.semnatics.relaxed_stop_words"));
        return stopWordsList;
    }

    /**
     * Clean up errors in graph (passed graph will be changed by reference):
     *  - remove multiple edges of the same type between two nodes
     *  - fix missing second synonym edge (opposite direction) between two nodes
     * @param graph
     */
    public static void sanitizeGraph(Graph<String, ConceptEdge> graph) {
        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator(graph, GraphUtils._CONCEPT_ROOT_NODE);
        while (iterator.hasNext()) {
            String nextVertex = iterator.next();
            Set<String> directChildren = new HashSet<>();
            Set<ConceptEdge> edgesOutgoing = graph.outgoingEdgesOf(nextVertex);
            for (ConceptEdge edge : edgesOutgoing) {
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

                        //now check situation when there is only one synonym edge but there is no corresponding one with different direction
                        //now we found syn edge "nextVertex -syn-> directChildVertex". Checking for "nextVertex <-syn- directChildVertex"
                        Set<ConceptEdge> edgesDifferentDirection = graph.getAllEdges(directChildVertex, nextVertex);
                        boolean isSynonymEdgeFound = false;
                        for (ConceptEdge edgeDifferentDirection : edgesDifferentDirection) {
                            if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edgeDifferentDirection.getRelationType())) {
                                isSynonymEdgeFound = true;
                                break;
                            }
                        }
                        if (!isSynonymEdgeFound) {
                            ConceptEdge newSynonymEdge = new ConceptEdge(edge.getRelationType(),
                                    (String) edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY),
                                    (String) edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME));
                            newSynonymEdge.setEdgeSourceDestination(directChildVertex, nextVertex);
                            graph.addEdge(directChildVertex, nextVertex, newSynonymEdge);
                        }
                    }
                }
            }
        }
    }

    public static class SemanticCategoryEnrichmentObject {

        public static String HYPERNYM_RELATION_TYPE = "HN";
        public static String SYNONYM_RELATION_TYPE = "SN";

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getType() {
            return type;
        }

        public boolean isKeepExistingEdges() {
            return isKeepExistingEdges;
        }

        String source;
        String target;
        String type;
        boolean isKeepExistingEdges;
    }
}
