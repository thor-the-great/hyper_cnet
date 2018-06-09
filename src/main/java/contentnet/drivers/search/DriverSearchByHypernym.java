package contentnet.drivers.search;

import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DriverSearchByHypernym {

    public static void main(String[] args) {
        DriverSearchByHypernym driver = new DriverSearchByHypernym();
        driver.doWork();
    }

    void doWork() {
        String hypernym = "base";
        //hypernym = "view";

        //hypernym = "riflescope";
        //hypernym = "rifle";
        //hypernym = "gun";
        //hypernym = "weapon";
        //hypernym = "instrument";
        //hypernym = "device";

        //hypernym = "keyboard";
        //hypernym = "key";
        //hypernym = "button";
        //hypernym = "writing";
        hypernym = "computer";
        //hypernym = "device";



        Graph<String, DefaultEdge> wordGraph = new DefaultDirectedGraph<>(DefaultEdge.class);
        GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_3_CSV_FILE_PATH);

        if (!wordGraph.containsVertex(hypernym)) {
            System.out.println("Word is not in concept graph");
            return;
        }

        GraphIterator<String, DefaultEdge> iterator = new DepthFirstIterator<>(wordGraph, hypernym);
        List<GraphPath<String, DefaultEdge>> sortedPath = new ArrayList<>();
        while(iterator.hasNext()) {
            String nextVertex =  iterator.next();
            if (wordGraph.outDegreeOf(nextVertex) == 0){
                GraphPath<String, DefaultEdge> path = DijkstraShortestPath.findPathBetween(wordGraph, hypernym, nextVertex);
                sortedPath.add(path);
                //System.out.println(hypernym +" => " +nextVertex + ", path length = " + (path == null? 0: path.getLength()));
            }
        }

        Collections.sort(sortedPath, Comparator.comparingInt(GraphPath::getLength));

        for ( GraphPath<String, DefaultEdge> path : sortedPath) {
            System.out.println(hypernym +" => " + path.getEndVertex() + ", path length = " + (path == null? 0: path.getLength()));
        }
    }
}
