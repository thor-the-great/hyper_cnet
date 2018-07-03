package contentnet.graph;

import org.jgrapht.Graph;
import org.jgrapht.io.CSVFormat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConceptGraphCSVImporterSimple {

    public void importGraph(Graph<String, ConceptEdge> graph, String filePath) {
        importGraph(graph, filePath, CSVFormat.ADJACENCY_LIST);
    }

    public void importGraph(Graph<String, ConceptEdge> graph, String filePath, CSVFormat format) {
        try {
            Scanner fileScanner = new Scanner(new FileInputStream(filePath));
            if (CSVFormat.ADJACENCY_LIST.equals(format)) {
                while (fileScanner.hasNext()) {
                    String vertexLine = fileScanner.nextLine();
                    //lineNum++;
                    //every odd line is a vertex line
                    //every even line is a edge line
                    //read two lines at one and create vertex and link them with edges
                    String[] vertexArray = vertexLine.split(",");
                    //this is final child on the three, no edge line will be provided
                    if (vertexArray.length == 1) {
                        if (!graph.containsVertex(vertexArray[0]))
                            graph.addVertex(vertexArray[0]);
                        continue;
                    }
                    if (vertexArray.length > 1) {
                        String edgeLine = fileScanner.nextLine();
                        String[] edgesArray = edgeLine.split(",");
                        String rootVertex = vertexArray[0];
                        if (!graph.containsVertex(rootVertex))
                            graph.addVertex(rootVertex);
                        for (int vertexCount = 1; vertexCount < vertexArray.length; vertexCount++) {
                            String nextVertex = vertexArray[vertexCount];
                            if (!graph.containsVertex(nextVertex))
                                graph.addVertex(nextVertex);
                            String nextEdgeString = edgesArray[vertexCount - 1];
                            String[] edgeAttrValues = nextEdgeString.split(":");
                            ConceptEdge edge = null;
                            if (ConceptEdge._RELATION_TYPE_GENERIC.equals(edgeAttrValues[0])) {
                                edge = new ConceptEdge(edgeAttrValues[0]);
                                edge.setEdgeSourceDestination(rootVertex, nextVertex);
                                graph.addEdge(rootVertex, nextVertex, edge);
                            } else if (ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edgeAttrValues[0])) {
                                edge = getConceptEdge(rootVertex, nextVertex, edgeAttrValues);
                                graph.addEdge(rootVertex, nextVertex, edge);
                            } else if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edgeAttrValues[0])) {
                                edge = getConceptEdge(rootVertex, nextVertex, edgeAttrValues);
                                graph.addEdge(rootVertex, nextVertex, edge);
                            }
                        }
                    }
                }
            }
            else if (CSVFormat.MATRIX.equals(format)) {
                int lineNum = 0;
                graph.addVertex(GraphUtils._CONCEPT_ROOT_NODE);
                while (fileScanner.hasNext()) {
                    String vertexLine = fileScanner.nextLine();
                    lineNum++;
                    //skip first line, it has only column names
                    if (lineNum == 1)
                        continue;
                    String[] exportValues = vertexLine.split(Character.toString(ConceptGraphCSVExport.DEFAULT_DELIMITER));

                    String source = exportValues[0];
                    if (!graph.containsVertex(source))
                        graph.addVertex(source);

                    String target = exportValues[2];
                    if (!graph.containsVertex(target))
                        graph.addVertex(target);

                    String categoryCode = exportValues[3];
                    String categoryName = exportValues[4];
                    String edgeTypeString = exportValues[1];
                    String edgeType = "";
                    if ("SN".equalsIgnoreCase(edgeTypeString)) {
                        edgeType = ConceptEdge._RELATION_TYPE_SYNONYM;
                    } else if ("HN".equalsIgnoreCase(edgeTypeString)) {
                        edgeType = ConceptEdge._RELATION_TYPE_HYPERNYM;
                    }
                    ConceptEdge relationEdge = new ConceptEdge(edgeType, categoryCode, categoryName);
                    relationEdge.setEdgeSourceDestination(source, target);
                    graph.addEdge(source, target, relationEdge);

                    boolean isRootConnected = Boolean.parseBoolean(exportValues[5]);
                    if (isRootConnected) {
                        ConceptEdge generalEdge = new ConceptEdge(ConceptEdge._RELATION_TYPE_GENERIC);
                        graph.addEdge(GraphUtils._CONCEPT_ROOT_NODE, source, generalEdge);
                    }
                }
            }
            fileScanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private ConceptEdge getConceptEdge(String rootVertex, String nextVertex, String[] edgeAttrValues) {
        ConceptEdge edge;Map<String, String> edgeAttrs = new HashMap<>();
        if (edgeAttrValues.length > 1) {
            for (int i = 1; i < edgeAttrValues.length; i++) {
                String[] attrValue = edgeAttrValues[i].split("=");
                edgeAttrs.put(attrValue[0], attrValue[1]);
            }
        }
        edge = new ConceptEdge(edgeAttrValues[0], edgeAttrs.get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY), edgeAttrs.get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME));
        edge.setEdgeSourceDestination(rootVertex, nextVertex);
        return edge;
    }
}
