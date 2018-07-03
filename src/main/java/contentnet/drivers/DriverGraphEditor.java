package contentnet.drivers;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import contentnet.GlobalProperties;
import contentnet.graph.ConceptEdge;
import contentnet.graph.ConceptGraphCSVImporterSimple;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.CSVFormat;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.Files.walk;

public class DriverGraphEditor {

    public static void main (String[] args) {
        DriverGraphEditor driver = new DriverGraphEditor();
        Graph<String, ConceptEdge> wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
        //GraphUtils.importGraph(wordGraph,GraphUtils._INWORK_1_CSV_FILE_PATH);
        //GraphUtils.importGraph(wordGraph, GraphUtils._DEFAULT_CSV_FILE_PATH);show
        //ConceptGraphCSVImporterSimple graphImporter = new ConceptGraphCSVImporterSimple();
        String importFromFile = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\wordGraph_43211804.csv.proc";
        //graphImporter.importGraph(wordGraph, importFromFile);
        //GraphUtils.importGraphAdjList(wordGraph, importFromFile);
        //String importFromFile = "C:\\work\\ariba\\matrix_export_graph.csv";
        GraphUtils.importGraphMatrix(wordGraph, importFromFile);
        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator(wordGraph, GraphUtils._CONCEPT_ROOT_NODE);
        //boolean categoryCaptured = false;
        String sourceVertex = GraphUtils._CONCEPT_ROOT_NODE;
        String unspscCategory = "", unspscCategoryName = "";
        while (iterator.hasNext()) {
            String nextVertex = iterator.next();
            Set<ConceptEdge> edges = wordGraph.getAllEdges(sourceVertex, nextVertex);
            for (ConceptEdge edge: edges) {
                if(ConceptEdge._RELATION_TYPE_SYNONYM.equals(edge.getRelationType()) || ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edge.getRelationType())) {
                    unspscCategory = (String)edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY);
                    unspscCategoryName = (String)edge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME);
                    break;
                }
            }
            sourceVertex = nextVertex;
        }
        System.out.println("Graph imported");
        System.out.println(getInfoString(wordGraph));
        Stack<String> commandStack = new Stack<>();
        Scanner scanner = new Scanner( System.in );
        boolean isRepeatingCommand = false;
        while(isRepeatingCommand || scanner.hasNext()) {
            String nextCommand = "";
            if (!isRepeatingCommand)
                nextCommand = scanner.nextLine();
            else {
                nextCommand = commandStack.peek();
                isRepeatingCommand = false;
            }
            commandStack.push(nextCommand);
            if ("q".equals(nextCommand)) {
                System.out.println("Exiting...");
                break;
            }
            else if ("rep".equalsIgnoreCase(nextCommand)) {
                //pop rep command itself
                if ("rep".equalsIgnoreCase(commandStack.peek())) {
                    commandStack.pop();
                }
                if (commandStack.empty()) {
                    System.out.println("History is empty, cancelling");
                    continue;
                } else {
                    String prevCommand = commandStack.peek();
                    System.out.println("Repeating previous command: " + prevCommand);
                    isRepeatingCommand = true;
                }
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
                        System.out.println("Edge removed : " + edge);
                    } else {
                        System.out.println("Cancelled");
                        continue;
                    }
                } else {
                    System.out.println("No edge found");
                }
            } else if ("rev".equals(nextCommand)) {
                System.out.println("Removing target vertex and connected edge");
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
                System.out.println("Removing vertex and all incoming edges");
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
            } else if ("rvt".equals(nextCommand)) {
                System.out.println("Removing vertex tree from the root");
                System.out.println("Enter vertex: ");
                String vertex = scanner.nextLine();
                if (wordGraph.containsVertex(vertex)) {
                    System.out.println("Vertex found ");
                    System.out.println("Confirm removal");
                    if ("y".equals(scanner.nextLine())) {
                        removeVertexTree(wordGraph, vertex, false);
                    } else {
                        System.out.println("Cancelled");
                        continue;
                    }
                }

            } else if ("rvtall".equals(nextCommand)) {
                System.out.println("Removing vertex tree from the root");
                System.out.println("Enter vertex: ");
                String vertex = scanner.nextLine();
                if (wordGraph.containsVertex(vertex)) {
                    System.out.println("Vertex found ");
                    System.out.println("Confirm removal");
                    if ("y".equals(scanner.nextLine())) {
                        removeVertexTree(wordGraph, vertex, true);
                    } else {
                        System.out.println("Cancelled");
                        continue;
                    }
                }
            } else if ("rsyn".equals(nextCommand)) {
                removeSynonymVertex(wordGraph, scanner);
            }else if ("rtv_wsyn".equals(nextCommand)) {
                System.out.println("Removing vertex tree plus outgoing edges, plus linked synonyms");
                System.out.println("Enter vertex: ");
                String vertex = scanner.nextLine();
                if (wordGraph.containsVertex(vertex)) {
                    System.out.println("Vertex found");
                    System.out.println("Confirm removal");
                    if ("y".equals(scanner.nextLine())) {
                        removeVertexWithNestedSynonyms(wordGraph, vertex);
                    } else
                        System.out.println("Cancelled");
                } else {
                    System.out.println("No such vertex, cancelled");
                }
            } else if ("rtv_oe".equalsIgnoreCase(nextCommand)) {
                System.out.println("Removing vertex tree plus outgoing edges");
                System.out.println("Enter vertex: ");
                String vertex = scanner.nextLine();
                if (wordGraph.containsVertex(vertex)) {
                    System.out.println("Vertex found");
                    System.out.println("Confirm removal");
                    if ("y".equals(scanner.nextLine())) {
                        Set<ConceptEdge> outgoingEdges = wordGraph.outgoingEdgesOf(vertex);
                        Set<String> targetVertexes = new HashSet<>();
                        for (ConceptEdge edge: outgoingEdges) {
                            targetVertexes.add(edge.getTarget());
                        }
                        for (String target : targetVertexes) {
                            wordGraph.removeEdge(vertex, target);
                        }
                        removeVertexTree(wordGraph, vertex, true);
                    } else
                        System.out.println("Cancelled");
                } else {
                    System.out.println("No such vertex, cancelled");
                }
            }
            else if ("show".equals(nextCommand)) {
                GraphUtils.displayGraph(wordGraph);
            } else if ("addv".equals(nextCommand)) {
                System.out.println("Enter new vertex:");
                String vertex = scanner.nextLine();
                if(wordGraph.containsVertex(vertex)) {
                    System.out.println("Vertex already exist");
                } else {
                    wordGraph.addVertex(vertex);
                    System.out.println("Want to add hypernym edge? (y/n)");
                    if ("y".equals(scanner.nextLine())) {
                        System.out.println("Enter source:");
                        String source = scanner.nextLine();
                        if (!wordGraph.containsVertex(source)) {
                            System.out.println("Source vertex must exist, cancelling");
                        } else {
                            ConceptEdge newEdge = new ConceptEdge(ConceptEdge._RELATION_TYPE_HYPERNYM, unspscCategory, unspscCategoryName);
                            wordGraph.addEdge(source, vertex, newEdge);
                            System.out.println("New edge added");
                        }
                    }
                    else {
                        System.out.println("Finished");
                    }
                }
            } else if ("exp".equalsIgnoreCase(nextCommand)) {
                System.out.println("Enter filepath for graph export file (empty means same file)");
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
                System.out.println("Graph saved");
            } else if ("expm".equalsIgnoreCase(nextCommand)) {
                    System.out.println("Export graph as MATRIX");
                    System.out.println("Enter filepath for graph export file (empty means same file)");
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
                    GraphUtils.exportGraph(filePath, wordGraph, CSVFormat.MATRIX);
                    System.out.println("Graph saved");
            } else if ("imp".equalsIgnoreCase(nextCommand)) {
                System.out.println("Importing new graph. Replace current one in memory (y/n)?");
                if ("y".equalsIgnoreCase(scanner.nextLine())) {
                    System.out.println("Enter new graph file name");
                    String newGraphFile = scanner.nextLine();
                    wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
                    GraphUtils.importGraphMatrix(wordGraph, newGraphFile);
                    System.out.println("Graph imported : " + newGraphFile);
                    System.out.println(getInfoString(wordGraph));
                    importFromFile = newGraphFile;
                } else {
                    System.out.println("Cancelling, leave current graph in memory");
                }
            } else if("convert".equalsIgnoreCase(nextCommand)) {
                System.out.println("Convert graph from adj to matrix. Enter source graph file : ");
                String newGraphFile = scanner.nextLine();
                wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
                GraphUtils.importGraphAdjList(wordGraph, newGraphFile);
                if (wordGraph.vertexSet().size() > 1)
                    System.out.println("Graph imported successfully ");
                System.out.println("Enter destination file : ");
                String exportFileName = scanner.nextLine();
                GraphUtils.exportGraph(exportFileName, wordGraph, CSVFormat.MATRIX);
                System.out.println("Graph exported");
            }
            else if ("info".equalsIgnoreCase(nextCommand)) {
                System.out.println(getInfoString(wordGraph));
            } else if ("ref".equalsIgnoreCase(nextCommand)) {
                System.out.println("Confirm refresh, all current edits will be lost (y/n):");
                if ("y".equals(scanner.nextLine())) {
                    wordGraph = new DefaultDirectedGraph<>(ConceptEdge.class);
                    GraphUtils.importGraphMatrix(wordGraph, importFromFile);
                    GraphUtils.displayGraph(wordGraph);
                } else
                    System.out.println("Cancelled");
            }else if ("final".equals(nextCommand)) {
                System.out.println("Finalizing results. Please check filename and confirm (y/n)");
                System.out.println(importFromFile);
                if("y".equalsIgnoreCase(scanner.nextLine())) {

                    String procFileName = importFromFile.substring(importFromFile.lastIndexOf("\\") + 1);
                    String noProcFileName = procFileName.substring(0, procFileName.indexOf(".proc"));

                    String categoryProcessed = noProcFileName.split("_|\\.")[1];

                    String unprocFileName = noProcFileName.substring(0, noProcFileName.lastIndexOf(".")) + "_unproc" + noProcFileName.substring(noProcFileName.lastIndexOf("."));

                    try {
                        //first create a master copy (if doesn't exist yet)
                        Files.move(
                                Paths.get(GlobalProperties.getSetting("result.raw_graph.folder") + "\\" + noProcFileName),
                                Paths.get(GlobalProperties.getSetting("result.processed_master_copies.folder") + "\\" + unprocFileName));

                        //now copy processed piece to proc folder
                        Files.move(
                                Paths.get(importFromFile),
                                Paths.get(GlobalProperties.getSetting("result.processed.folder") + "\\" + noProcFileName)
                        );

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try (Stream<Path> paths = walk(Paths.get(GlobalProperties.getSetting("result.hw_collected.folder")), 1)) {

                        Optional<Path> matchedFilePath = paths.filter(Files::isRegularFile)
                                .filter(filePath ->filePath.getFileName().toString().endsWith(".txt") && filePath.getFileName().toString().startsWith("hwByCategory_" + categoryProcessed + "_"))
                                .findFirst();
                        if (matchedFilePath.isPresent()) {
                            try {
                                Files.move(
                                        matchedFilePath.get(),
                                        Paths.get(GlobalProperties.getSetting("result.hw_collected.processed.folder") + "\\" + matchedFilePath.get().getFileName())
                                );
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //now refresh statistics
                    DriverAnalyseForStopWords stopWordsDriver = new DriverAnalyseForStopWords();
                    stopWordsDriver.doWork();

                    DriverProgressStat progressDriver = new DriverProgressStat();
                    progressDriver.doWork();

                    System.out.println("Done");
                }
                else {
                    System.out.println("Cancelling...");
                }
            }else
            {
                System.out.println("unknown command :(");
            }
        }
        //GraphUtils.displayGraph(wordGraph);
        //GraphUtils.exportGraph("C:\\work\\ariba\\export_test_expr.csv", wordGraph);
    }

    public static void removeVertexWithNestedSynonyms(Graph<String, ConceptEdge> wordGraph, String vertex) {
        Set<ConceptEdge> outgoingEdges = wordGraph.outgoingEdgesOf(vertex);
        Set<String> synVertexes = new HashSet<>();
        for (ConceptEdge edge: outgoingEdges) {
            if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edge.getRelationType()))
                synVertexes.add(edge.getTarget());
        }
        for (String synVertex: synVertexes) {
            doRemoveSyn(wordGraph, synVertex);
        }

        Set<String> targetVertexes = new HashSet<>();
        for (ConceptEdge edge: outgoingEdges) {
            targetVertexes.add(edge.getTarget());
        }
        for (String target : targetVertexes) {
            wordGraph.removeEdge(vertex, target);
        }
        removeVertexTree(wordGraph, vertex, true);
    }

    private static String getInfoString(Graph<String, ConceptEdge> wordGraph) {
        StringBuilder sb = new StringBuilder();
        sb.append("vertexes #: ").append(wordGraph.vertexSet().size()).append(", ");
        sb.append("edges #: ").append(wordGraph.edgeSet().size());
        return sb.toString();
    }

    private static void removeSynonymVertex(Graph<String, ConceptEdge> wordGraph, Scanner scanner) {
        System.out.println("Removing synonym vertex with all in/out edges");
        System.out.println("Enter vertex: ");
        String vertex = scanner.nextLine();
        if (wordGraph.containsVertex(vertex)) {
            System.out.println("Vertex found ");
            System.out.println("Confirm removal");
            if ("y".equals(scanner.nextLine())) {
                doRemoveSyn(wordGraph, vertex);
                System.out.println("Removed");
            } else
                System.out.println("Cancelled");
        } else {
            System.out.println("No such vertex, cancelled");
        }
    }

    private static void doRemoveSyn(Graph<String, ConceptEdge> wordGraph, String vertex) {
        Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(vertex);
        Set<String> sourceVertexes = new HashSet<>();
        for (ConceptEdge edge: incomingEdges) {
            if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edge.getRelationType())) {
                sourceVertexes.add(edge.getSource());
            }
        }
        for (String source : sourceVertexes) {
            wordGraph.removeEdge(source, vertex);
        }

        Set<ConceptEdge> outgoingEdges = wordGraph.outgoingEdgesOf(vertex);
        Set<String> targetVertexes = new HashSet<>();
        for (ConceptEdge edge: outgoingEdges) {
            if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edge.getRelationType())) {
                targetVertexes.add(edge.getTarget());
            }
        }
        for (String target : targetVertexes) {
            wordGraph.removeEdge(vertex, target);
        }
        wordGraph.removeVertex(vertex);
    }

    private static void removeVertexTree(Graph<String, ConceptEdge> wordGraph, String vertex, boolean isDeleteAll) {
        if (GraphUtils._CONCEPT_ROOT_NODE.equals(vertex))
            return ;
        Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(vertex);

        if (!isDeleteAll) {
            if (incomingEdges.size() != 1)
                return;
            String sourceVertex = incomingEdges.iterator().next().getSource();
            wordGraph.removeEdge(sourceVertex, vertex);
            wordGraph.removeVertex(vertex);
            System.out.println("Removed vertex " + vertex);
            removeVertexTree(wordGraph, sourceVertex, isDeleteAll);
        } else {
            Set<String> sourceVertexes = new HashSet<>();

            for (ConceptEdge edge: incomingEdges) {
                sourceVertexes.add(edge.getSource());
            }

            for (String sourceVertex : sourceVertexes) {
                wordGraph.removeEdge(sourceVertex, vertex);
            }
            wordGraph.removeVertex(vertex);
            for (String sourceVertex : sourceVertexes) {
                removeVertexTree(wordGraph, sourceVertex, isDeleteAll);
            }
            System.out.println("Removed vertex " + vertex);
        }
    }
}
