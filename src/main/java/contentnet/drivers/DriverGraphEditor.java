package contentnet.drivers;

import contentnet.graph.ConceptEdge;
import contentnet.graph.ConceptGraphCSVImporterSimple;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class DriverGraphEditor {

    public static void main (String[] args) {
        DriverGraphEditor driver = new DriverGraphEditor();
        Graph<String, ConceptEdge> wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
        //GraphUtils.importGraph(wordGraph,GraphUtils._INWORK_1_CSV_FILE_PATH);
        //GraphUtils.importGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);
        ConceptGraphCSVImporterSimple graphImporter = new ConceptGraphCSVImporterSimple();
        String importFromFile = "C:\\work\\ariba\\export_test_expr.csv";
        graphImporter.importGraph(wordGraph, importFromFile);

        System.out.println("Graph imported");
        Scanner scanner = new Scanner( System.in );
        while(scanner.hasNext()) {
            String nextCommand = scanner.nextLine();
            if ("q".equals(nextCommand)) {
                System.out.println("Exiting...");
                break;
            }
            else if ("re".equals(nextCommand)) {
                System.out.println("Removing edge");
                System.out.println("Enter source vertex: ");
                String source = scanner.nextLine();
                System.out.println("Enter target vertex: ");
                String target = scanner.nextLine();
                ConceptEdge edge = wordGraph.getEdge(source, target);
                if (edge != null) {
                    System.out.println("Edge found : " + edge );
                    System.out.println("Confirm removal");
                    if ("y".equals(scanner.nextLine())) {
                        wordGraph.removeEdge(edge);
                    } else {
                        System.out.println("Cancelled");
                        continue;
                    }
                } else {
                    System.out.println("No edge found");
                }
            } else if ("rev".equals(nextCommand)) {
                System.out.println("Removing edge and vertex");
                System.out.println("Enter source vertex: ");
                String source = scanner.nextLine();
                System.out.println("Enter target vertex: ");
                String target = scanner.nextLine();
                ConceptEdge edge = wordGraph.getEdge(source, target);
                if (edge != null) {
                    System.out.println("Edge found : " + edge );
                    System.out.println("Confirm removal with target vertex");
                    if ("y".equals(scanner.nextLine())) {
                        wordGraph.removeEdge(edge);
                        wordGraph.removeVertex(target);
                        System.out.println("Removed");
                        continue;
                    } else {
                        System.out.println("Cancelled");
                        continue;
                    }
                } else {
                    System.out.println("No edge found");
                }
            } else if ("rv".equals(nextCommand)) {
                System.out.println("Removing vertex");
                System.out.println("Enter vertex: ");
                String vertex = scanner.nextLine();
                if (wordGraph.containsVertex(vertex)) {
                    System.out.println("Vertex found ");
                    System.out.println("Confirm removal");
                    if ("y".equals(scanner.nextLine())) {
                        Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(vertex);
                        Set<String> sourceVertexes = new HashSet<>();
                        for (ConceptEdge edge: incomingEdges) {
                            sourceVertexes.add(edge.getSource());
                        }
                        for (String source : sourceVertexes) {
                            wordGraph.removeEdge(source, vertex);
                        }
                        wordGraph.removeVertex(vertex);
                        System.out.println("Removed");
                        continue;
                    } else {
                        System.out.println("Cancelled");
                        continue;
                    }
                }
            }
            else if ("show".equals(nextCommand)) {
                GraphUtils.displayGraph(wordGraph);
            }
            else if ("exp".equals(nextCommand)) {
                String filePath = scanner.nextLine();
                if ("".equals(filePath)) {
                    System.out.println("Confirm overwrite");
                    if (!"y".equals(scanner.nextLine())) {
                        System.out.println("Cancelled");
                        continue;
                    } else {
                        filePath = importFromFile;
                    }
                }
                GraphUtils.exportGraph(filePath, wordGraph);
            }
        }
        GraphUtils.displayGraph(wordGraph);
        //GraphUtils.exportGraph("C:\\work\\ariba\\export_test_expr.csv", wordGraph);
    }
}
