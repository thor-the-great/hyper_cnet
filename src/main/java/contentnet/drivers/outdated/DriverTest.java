package contentnet.drivers.outdated;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGraphModel;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraphView;
import contentnet.NltkAPI;
import contentnet.drivers.DriverGraphEditor;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.event.GraphVertexChangeEvent;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.CSVFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

/**
 * Testing different java approaches, not relevant for any functions
 */
public class DriverTest {

    void doWork() {
    }

    public static void main(String[] args) {
        //reads folder and filter file names as per pattern, put those filtered names as strings to the list
        /*String hwByCategoriesExportFolder = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\";
        List<String> processedCategories = new ArrayList<>();
        try(Stream<Path> filePaths = Files.walk(Paths.get(hwByCategoriesExportFolder))) {
            filePaths.filter(Files::isRegularFile).
                    filter(filePath -> filePath.getFileName().toString().startsWith("hwByCategory") && filePath.getFileName().toString().endsWith("txt")).
                    forEach(filePath -> processedCategories.add(filePath.getFileName().toString().split("_")[1]));
        } catch (IOException e) {
            e.printStackTrace();
        }
        processedCategories.forEach(System.out::println);*/

        /*String[] params = new String[4];
        params[0] = "C:\\dev\\python\\anaconda\\envs\\wordnet_nltk\\python.exe";
        params[1] = "C:\\work\\ariba\\wordnet_nltk\\wn_test1.py";
        params[2] = "lcd";
        params[3] = "computer_display";

        try {
            Process pythonScriptProcess = Runtime.getRuntime().exec(params);
            BufferedReader br = new BufferedReader(new InputStreamReader(pythonScriptProcess.getInputStream()));
            String scriptOutput = br.readLine();
            System.out.println(scriptOutput);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        /*List<String> result = NltkAPI.getInstance().getLowestCommonHypernyms("touchscreen", "computer_display");
        result.forEach(System.out::println);*/

        DriverTest driver = new DriverTest();
        driver.doWork();
    }

}
