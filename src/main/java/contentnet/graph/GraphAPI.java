package contentnet.graph;

import contentnet.category.UNSPSCRecord;
import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class GraphAPI {

    private static  GraphAPI instance;

    private GraphAPI() {

    }

    public static GraphAPI getInstance() {
        if (instance == null) {
            instance = new GraphAPI();
        }
        return instance;
    }

    public static void addEdgeToGraph(Graph<String, ConceptEdge> wordGraph, String source, String target, String edgeType, UNSPSCRecord categoryRecord) {
        addEdgeToGraph(wordGraph, source, target, edgeType, categoryRecord.getUnspsc(), categoryRecord.getUnspscName());
    }

    public static void addEdgeToGraph(Graph<String, ConceptEdge> wordGraph, String source, String target, String edgeType, String category, String categoryName) {
        if (source != null && target != null && source.equalsIgnoreCase(target))
            return;
        if (GraphUtils._CONCEPT_ROOT_NODE.equalsIgnoreCase(source)) {
            if (!wordGraph.containsEdge(GraphUtils._CONCEPT_ROOT_NODE, target)) {
                ConceptEdge newEdge = new ConceptEdge(ConceptEdge._RELATION_TYPE_GENERIC);
                wordGraph.addEdge(GraphUtils._CONCEPT_ROOT_NODE, target, newEdge);
                return;
            }
        }
        if (!wordGraph.containsEdge(source, target)) {
            ConceptEdge newEdge = new ConceptEdge(edgeType, category, categoryName);
            wordGraph.addEdge(source, target, newEdge);
            //special case for synonyms - need to add backward relation as well, because both words are equal
            if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(edgeType)) {
                if (!wordGraph.containsEdge(target, source)) {
                    ConceptEdge backloopSynonymRelationEdge = new ConceptEdge(edgeType, category, categoryName);
                    wordGraph.addEdge(target, source, backloopSynonymRelationEdge);
                }
            }
        }
    }

    /**
     * Merges one graph into another. After merge mainGraph graph will have all the vertexes, but edges will be proceed and potentially changed.
     * GraphtoMerge will remain unchanged
     *
     * @param mainGraph
     * @param graphToMerge
     * @return
     */
    public  Graph<String, ConceptEdge> mergeGraphs(Graph<String, ConceptEdge> mainGraph, Graph<String, ConceptEdge> graphToMerge) {
        if(mainGraph == null)
            return graphToMerge;
        if (graphToMerge == null)
            return mainGraph;
        if (mainGraph.vertexSet().size() == 0 || mainGraph.vertexSet().size() == 1)
            return graphToMerge;
        if (graphToMerge.vertexSet().size() <= 1) {
            return mainGraph;
        }
        DepthFirstIterator<String, ConceptEdge> dfsIterator = new DepthFirstIterator<>(graphToMerge, GraphUtils._CONCEPT_ROOT_NODE);
        while (dfsIterator.hasNext()) {
            String nextVertexGToMerge = dfsIterator.next();
            if(GraphUtils._CONCEPT_ROOT_NODE.equals(nextVertexGToMerge))
                continue;
            if (!mainGraph.vertexSet().contains(nextVertexGToMerge)) {
                mainGraph.addVertex(nextVertexGToMerge);
            }
            Set<ConceptEdge> edgesGToMerge = graphToMerge.incomingEdgesOf(nextVertexGToMerge);
            for (ConceptEdge edgeGToMerge : edgesGToMerge) {
                if (ConceptEdge._RELATION_TYPE_GENERIC.equals(edgeGToMerge.getRelationType()))
                    continue;
                String edgeSource = edgeGToMerge.getSource();
                if (!mainGraph.containsVertex(edgeSource))
                    mainGraph.addVertex(edgeSource);
                ConceptEdge newEdge = (ConceptEdge) edgeGToMerge.clone();
                mainGraph.addEdge(edgeSource, edgeGToMerge.getTarget(), newEdge);
            }
        }
        return mainGraph;
    }

    /**
     * Fix cycles so essentially all vertexes inside the cycle became synonyms. Algorithms is following - lower level vertex from cycle remains as is,
     * for all others "inside" the cycle we change edge type from Hypernym to Synonym with all other attributes the same.
     *
     * @param categoryRecord
     * @param wordGraph
     */
    public void fixCycles(UNSPSCRecord categoryRecord, Graph<String, ConceptEdge> wordGraph) {
        CycleDetector<String, ConceptEdge> cycleDetector = new CycleDetector<>(wordGraph);
        Set<String> cycleVertices = cycleDetector.findCycles();
        if (cycleVertices != null && cycleVertices.size() > 0) {
            Set<ConceptEdge> edgesToRemove = new HashSet<>();
            Set<ConceptEdge> edgesToAdd = new HashSet<>();
            for (Iterator<String> it = cycleVertices.iterator(); it.hasNext(); ) {
                String cycleVertex = it.next();
                Set<ConceptEdge> incomingEdges = wordGraph.incomingEdgesOf(cycleVertex);
                for (Iterator<ConceptEdge> iteratorEdges = incomingEdges.iterator(); iteratorEdges.hasNext();) {
                    ConceptEdge edge = iteratorEdges.next();
                    String edgeSource = edge.getSource();
                    if (ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edge.getRelationType()) && cycleVertices.contains(edgeSource)) {
                        //System.out.println("Vertex in cycle : " + cycleVertex);
                        edgesToRemove.add(edge);
                        ConceptEdge synonymEdge = new ConceptEdge(ConceptEdge._RELATION_TYPE_SYNONYM, categoryRecord.getUnspsc(), categoryRecord.getUnspscName());
                        synonymEdge.setEdgeSourceDestination(edgeSource, cycleVertex);
                        edgesToAdd.add(synonymEdge);
                    }
                }
            }
            for(Iterator<ConceptEdge> itEdgedToRemove = edgesToRemove.iterator(); itEdgedToRemove.hasNext();) {
                wordGraph.removeEdge(itEdgedToRemove.next());
            }
            for(Iterator<ConceptEdge> itEdgedToAdd = edgesToAdd.iterator(); itEdgedToAdd.hasNext();) {
                ConceptEdge newEdge = itEdgedToAdd.next();
                wordGraph.addEdge(newEdge.getEdgeSource(), newEdge.getEdgeDestination(), newEdge);
            }
        }
    }
}
