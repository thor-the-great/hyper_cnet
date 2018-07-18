package contentnet.drivers;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraphView;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphAPI;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.ListenableGraph;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.DefaultListenableGraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.CSVFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Testing different java approaches, not relevant for any functions
 */
public class DriverGUIEditor {

    String workDir = System.getProperty("user.dir");
    JFrame frame;

    void doWork() {
        Graph<String, ConceptEdge> wordGraph = new DirectedMultigraph<>(ConceptEdge.class);
        //String fileName = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\interim_copies\\wordGraph_43211608_inwork.csv.proc12";
        String fileName = "";
        if (new File(fileName).exists()) {
            GraphUtils.importGraphMatrix(wordGraph, fileName);
        }
        ListenableGraph<String, ConceptEdge> lGraph = new DefaultListenableGraph(wordGraph);

        String[] categoryInfo = GraphUtils.getGraphCategoryInfo(wordGraph);
        String title = "Category " + categoryInfo[0] + " ( " + categoryInfo[1] + " )";

        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JGraphXAdapter<String, ConceptEdge> graphAdapter = new JGraphXAdapter<>(lGraph);

        //mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
        layout.execute(graphAdapter.getDefaultParent());

        mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
        mxGraphView view =graphAdapter.getView();
        view.setScale(2.5f);

        List<String> selectedVertexes = new ArrayList<>();
        List<ConceptEdge> selectedEdges = new ArrayList<>();

        DefaultListModel listModel = new DefaultListModel();
        JList commandList = new JList(listModel); //data has type Object[]
        commandList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        commandList.setModel(listModel);

        JSplitPane inner = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                graphComponent, commandList);
        inner.setDividerLocation(1200);

        graphComponent.getGraphControl().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mxCell cell = (mxCell) graphComponent.getCellAt(e.getX(), e.getY());
                if (cell != null && cell.isVertex()) {
                    System.out.println(cell.getValue());
                    selectedVertexes.add(cell.getValue().toString());
                    listModel.addElement(cell.getValue().toString());
                }
                else if (cell != null && cell.isEdge()) {
                    System.out.println(cell.getValue());
                    ConceptEdge edge = (ConceptEdge) cell.getValue();
                    selectedEdges.add(edge);
                    String selectionString = edge.getSource() + " -> " + edge.getTarget() + " " + edge.getRelationType();
                    listModel.addElement(selectionString);
                }
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
            }
        });

        JPanel  buttonsPane = new JPanel(new FlowLayout());

        JButton addNewEdgeButton = new JButton("Add edge");
        addNewEdgeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Add edge");

                Object[] relationTypes = {ConceptEdge._RELATION_TYPE_HYPERNYM, ConceptEdge._RELATION_TYPE_SYNONYM, ConceptEdge._RELATION_TYPE_GENERIC};
                String edgeRelationType = (String)JOptionPane.showInputDialog(
                        frame,
                        "Type of edge relation",
                        "Create new edge",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        relationTypes,
                        ConceptEdge._RELATION_TYPE_HYPERNYM);

                if ((edgeRelationType != null) && (edgeRelationType.length() > 0)) {
                    addEdge(selectedVertexes, lGraph, graphAdapter, listModel, edgeRelationType);
                }

                //addEdge(selectedVertexes, lGraph, graphAdapter, listModel, ConceptEdge._RELATION_TYPE_HYPERNYM);
            }
        });

        JButton addVertexButton = new JButton("Add vertex");
        addVertexButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Add new vertex");
                String vertex = (String)JOptionPane.showInputDialog(
                        null,
                        "Enter vertex: ",
                        "Create vertex",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        "vertex");
                if (vertex == null || "".equals(vertex.trim())) {
                    JOptionPane.showMessageDialog(null, "Enter non-empty name of the vertex", "Can't create vertex", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                graphAdapter.getModel().beginUpdate();
                lGraph.addVertex(vertex);
                graphAdapter.getModel().endUpdate();
            }
        });

        JButton deleteVertexes = new JButton("Delete vertex");
        deleteVertexes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Delete vertexes");
                graphAdapter.getModel().beginUpdate();
                for (String selectedVertex: selectedVertexes) {
                    System.out.println(selectedVertex);
                    lGraph.removeVertex(selectedVertex);
                }
                graphAdapter.getModel().endUpdate();
                listModel.removeAllElements();
                selectedVertexes.clear();
                graphAdapter.repaint();
            }
        });

        JButton deleteVertexesWSyn = new JButton("Delete vertex with nested syns");
        deleteVertexesWSyn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Delete vertexes");
                graphAdapter.getModel().beginUpdate();
                for (String selectedVertex: selectedVertexes) {
                    System.out.println(selectedVertex);
                    DriverGraphEditor.removeVertexWithNestedSynonyms(lGraph, selectedVertex);
                }
                selectedVertexes.clear();
                listModel.removeAllElements();
                graphAdapter.getModel().endUpdate();
                graphAdapter.repaint();
            }
        });

        JButton deleteEdges = new JButton("Delete edges");
        deleteEdges.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Delete edges");
                graphAdapter.getModel().beginUpdate();
                for (ConceptEdge selectedEdge: selectedEdges) {
                    System.out.println(selectedEdge);
                    lGraph.removeEdge(selectedEdge);
                }
                selectedEdges.clear();
                listModel.removeAllElements();
                graphAdapter.getModel().endUpdate();
                graphAdapter.repaint();
            }
        });

        JButton loadNewGraphButton = new JButton("Load graph");

        MouseAdapter handler = new LoadGraphHandler(graphAdapter, wordGraph, lGraph);

        loadNewGraphButton.addMouseListener(handler);

        JButton clearSelectionButton = new JButton("Clear all selections");
        clearSelectionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Clear selection");
                selectedVertexes.clear();
                selectedEdges.clear();
                listModel.removeAllElements();
            }
        });

        JButton reloadGraphButton = new JButton("Refresh");
        reloadGraphButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Refresh");
                mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
                layout.execute(graphAdapter.getDefaultParent());
            }
        });

        JButton sanitizeGraphButton = new JButton("Sanitize");
        sanitizeGraphButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Sanitize");
                graphAdapter.getModel().beginUpdate();
                GraphUtils.sanitizeGraph(lGraph);
                graphAdapter.getModel().endUpdate();
                graphAdapter.repaint();
            }
        });

        JButton save = new JButton("Save");
        save.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                System.out.println("Save");
                if (workDir.equals(""))
                    workDir = workDir.equalsIgnoreCase("") ? System.getProperty("user.dir") : workDir;
                JFileChooser fc = new JFileChooser(workDir);
                int rc = fc.showDialog(null, "Save graph");
                if (rc == JFileChooser.APPROVE_OPTION) {
                    String exportFilename = fc.getSelectedFile().getAbsolutePath();
                    if (new File(exportFilename).exists()
                            && JOptionPane.showConfirmDialog(graphComponent, "Overwrite existing file?") != JOptionPane.YES_OPTION) {
                        return;
                    } else { 
                        GraphUtils.exportGraph(exportFilename, lGraph, CSVFormat.MATRIX);
                    }
                    workDir = fc.getSelectedFile().getParent();
                }
            }
        });

        buttonsPane.add(loadNewGraphButton);
        buttonsPane.add(addNewEdgeButton);
        buttonsPane.add(addVertexButton);
        buttonsPane.add(addVertexButton);
        buttonsPane.add(deleteVertexes);
        buttonsPane.add(deleteVertexesWSyn);
        buttonsPane.add(deleteEdges);
        buttonsPane.add(sanitizeGraphButton);
        buttonsPane.add(clearSelectionButton);
        buttonsPane.add(reloadGraphButton);
        buttonsPane.add(save);
        JSplitPane outer = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                inner, buttonsPane);
        outer.setDividerLocation(700);
        outer.setResizeWeight(1);
        outer.setDividerSize(5);
        outer.setBorder(null);

        frame.add(outer);

        frame.pack();
        frame.setSize(1500, 800);
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
    }

    private void addEdge(List<String> selectedVertexes, ListenableGraph<String, ConceptEdge> lGraph, JGraphXAdapter<String, ConceptEdge> graphAdapter, DefaultListModel listModel, String edgeType) {
        if (ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edgeType) || ConceptEdge._RELATION_TYPE_SYNONYM.equals(edgeType)) {
            if (selectedVertexes.size() != 2) {
                JOptionPane.showMessageDialog(null, "Two vertexes must be selected", "Can't create edge", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } else if (ConceptEdge._RELATION_TYPE_GENERIC.equals(edgeType)) {
            if (selectedVertexes.size() != 1) {
                JOptionPane.showMessageDialog(null, "Only one vertex must be selected", "Can't create edge", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        String sourceVertex = "", destinationVertex = "";
        if (ConceptEdge._RELATION_TYPE_HYPERNYM.equals(edgeType) || ConceptEdge._RELATION_TYPE_SYNONYM.equals(edgeType)) {
            sourceVertex = selectedVertexes.get(0);
            destinationVertex = selectedVertexes.get(1);
        } else {
            sourceVertex = GraphUtils._CONCEPT_ROOT_NODE;
            destinationVertex = selectedVertexes.get(0);
        }

        String[] categoryInfo = GraphUtils.getGraphCategoryInfo(lGraph);
        graphAdapter.getModel().beginUpdate();
        GraphAPI.addEdgeToGraph(lGraph, sourceVertex, destinationVertex, edgeType, categoryInfo[0], categoryInfo[1]);
        graphAdapter.getModel().endUpdate();
        selectedVertexes.clear();
        listModel.removeAllElements();
    }

    public static void main(String[] args) {
        DriverGUIEditor driver = new DriverGUIEditor();
        driver.doWork();
    }


    class LoadGraphHandler extends MouseAdapter {

        JGraphXAdapter<String, ConceptEdge> graphAdapter;
        Graph<String, ConceptEdge> wordGraph;
        ListenableGraph<String, ConceptEdge> lGraph;

        LoadGraphHandler(JGraphXAdapter<String, ConceptEdge> graphAdapter, Graph<String, ConceptEdge> wordGraph, ListenableGraph<String, ConceptEdge> lGraph) {
            this.graphAdapter = graphAdapter;
            this.wordGraph = wordGraph;
            this.lGraph = lGraph;
        }

        public void mouseClicked(MouseEvent mouseEvent) {
            System.out.println("Load graph");
            workDir = workDir.equalsIgnoreCase("") ? System.getProperty("user.dir") : workDir;
            JFileChooser fc = new JFileChooser(workDir);
            int rc = fc.showDialog(null, "Load graph");
            if (rc == JFileChooser.APPROVE_OPTION) {
                String selectedFilePath = fc.getSelectedFile().getAbsolutePath();
                workDir = fc.getSelectedFile().getParent();
                graphAdapter.getModel().beginUpdate();
                wordGraph = new DirectedMultigraph<>(ConceptEdge.class);
                GraphUtils.importGraphMatrix(wordGraph, selectedFilePath);
                //
                //lGraph = new DefaultListenableGraph<>(wordGraph);
                Set<String> vertexes = new HashSet<>();
                for (Iterator<String> it = lGraph.vertexSet().iterator(); it.hasNext(); ) {
                    vertexes.add(it.next());
                }
                for (String vertex : vertexes) {
                    lGraph.removeVertex(vertex);
                }
                System.out.println(lGraph.vertexSet());

                String category = "", categoryName = "";
                boolean isCategoryInfoInitialized = false;
                for (String vertex : wordGraph.vertexSet()) {
                    if (!lGraph.containsVertex(vertex)) {
                        lGraph.addVertex(vertex);
                    }

                    Set<ConceptEdge> outEdges = wordGraph.outgoingEdgesOf(vertex);
                    for (ConceptEdge edge : outEdges) {
                        String targetV = edge.getTarget();
                        if (!lGraph.containsVertex(targetV))
                            lGraph.addVertex(targetV);
                        ConceptEdge clonedEdge = (ConceptEdge) edge.clone();
                        if (!isCategoryInfoInitialized && !ConceptEdge._RELATION_TYPE_GENERIC.equals(clonedEdge.getRelationType())) {
                            category = (String) clonedEdge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY);
                            categoryName = (String) clonedEdge.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME);
                            isCategoryInfoInitialized = true;
                        }
                        lGraph.addEdge(vertex, targetV, clonedEdge);
                    }
                }

                System.out.println(lGraph.vertexSet());

                graphAdapter.getModel().endUpdate();
                mxIGraphLayout layout = new mxHierarchicalLayout(graphAdapter);
                layout.execute(graphAdapter.getDefaultParent());

                mxGraphView view = graphAdapter.getView();
                view.setScale(2.5f);
                String title = "";
                if (isCategoryInfoInitialized) {
                    title = "Category " + category + " ( " + categoryName + " )";
                } else {
                    title = "Category info N/A";
                }
                frame.setTitle(title);
            }

            //graphAdapter.repaint();
            //graphComponent.refresh();
        }
    }
}
