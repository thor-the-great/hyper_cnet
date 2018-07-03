package contentnet.graph;

import org.jgrapht.*;
import org.jgrapht.io.CSVFormat;
import org.jgrapht.io.ExportException;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConceptGraphCSVExport  {
        public static final char DEFAULT_DELIMITER = ',';

        //private final Set<CSVFormat.Parameter> parameters;
        private CSVFormat format;
        private char delimiter;

        /**
         * Creates a new CSVExporter with {@link CSVFormat#ADJACENCY_LIST} format and integer name
         * provider for the vertices.
         */
        public ConceptGraphCSVExport()
        {
            //this(new IntegerComponentNameProvider<>(), CSVFormat.ADJACENCY_LIST, DEFAULT_DELIMITER);
            this(CSVFormat.ADJACENCY_LIST);
        }

        public ConceptGraphCSVExport(CSVFormat exportFormat)
        {
            //this(new IntegerComponentNameProvider<>(), CSVFormat.ADJACENCY_LIST, DEFAULT_DELIMITER);
            format = exportFormat;
            delimiter = DEFAULT_DELIMITER;
        }
        /**
         * Exports a graph
         *
         * @param g the graph
         * @param writer the writer
         */
        //@Override
        public void exportGraph(Graph<String, ConceptEdge> g, Writer writer)
        {
            PrintWriter out = new PrintWriter(writer);
            switch (format) {
                //case EDGE_LIST:
                //    exportAsEdgeList(g, out);
                //    break;
                case ADJACENCY_LIST:
                    exportAsAdjacencyList(g, out);
                    break;
                case MATRIX:
                    exportAsMatrix(g, out);
                    break;
            }
            out.flush();
            out.close();
        }

        void exportGraph(Graph<String, ConceptEdge> g, File file) throws ExportException
        {
            try {
                exportGraph(g, new FileWriter(file));
            } catch (IOException e) {
                throw new ExportException(e);
            }
        }

        private void exportAsAdjacencyList(Graph<String, ConceptEdge> g, PrintWriter out)
        {
            StringBuilder sbVertexes = new StringBuilder();
            StringBuilder sbEdges = new StringBuilder();
            for (String v : g.vertexSet()) {
                //save vertex
                sbVertexes.append(escapeDSV(v, delimiter));
                for (ConceptEdge e : g.outgoingEdgesOf(v)) {
                    //save vertex
                    sbVertexes.append(delimiter);
                    sbVertexes.append(escapeDSV(e.getTarget(),delimiter));
                    //save edge
                    sbEdges.append(e.getRelationType());
                    sbEdges.append(":");
                    Map<String, Object> attributes = e.getAttributes();
                    int attrCount = 0;
                    for (String attrKey : attributes.keySet()) {
                        sbEdges.append(attrKey);
                        sbEdges.append("=");
                        sbEdges.append(attributes.get(attrKey));
                        attrCount++;
                        if (attrCount < attributes.size())
                            sbEdges.append(":");
                    }
                    sbEdges.append(delimiter);
                }
                out.print(sbVertexes.toString());
                out.println();
                out.print(sbEdges);
                if(sbEdges.length() > 0)
                    out.println();

                sbEdges.setLength(0);
                sbVertexes.setLength(0);
            }
        }

    private void exportAsMatrix(Graph<String, ConceptEdge> g, PrintWriter out)
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SOURCE").append(delimiter);
        sb.append("RELATION").append(delimiter);
        sb.append("TARGET").append(delimiter);
        sb.append("CATEGORY_CODE").append(delimiter);
        sb.append("CATEGORY_NAME").append(delimiter);
        sb.append("IS_CONNECTED_TO_ROOT").append(delimiter);
        out.print(sb.toString());
        out.println();
        sb.setLength(0);
        for (String v : g.vertexSet()) {
            //save vertex
            if (GraphUtils._CONCEPT_ROOT_NODE.equalsIgnoreCase(v))
                continue;
            //StringBuilder sb = new StringBuilder();
            for (ConceptEdge e : g.outgoingEdgesOf(v)) {
                //sb.setLength(0);
                //first goes the source vertex
                sb.append(escapeDSV(v, delimiter)).append(delimiter);
                //type of egde
                if (ConceptEdge._RELATION_TYPE_HYPERNYM.equals(e.getRelationType())) {
                    sb.append("HN");
                } else if (ConceptEdge._RELATION_TYPE_SYNONYM.equals(e.getRelationType())) {
                    sb.append("SN");
                }
                sb.append(delimiter);
                //target vertex
                sb.append(escapeDSV(e.getTarget(), delimiter));
                sb.append(delimiter);
                //category code
                sb.append(escapeDSV((String) e.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY), delimiter));
                sb.append(delimiter);
                //category name
                sb.append(escapeDSV((String) e.getAttributes().get(ConceptEdge._ATTR_KEY_UNSPSC_CATEGORY_NAME), delimiter));
                sb.append(delimiter);
                //flag that this is connected to root node
                Set<ConceptEdge> incomingEdges = g.incomingEdgesOf(v);
                boolean isRootConnected = false;
                for (ConceptEdge edge : incomingEdges) {
                    if (GraphUtils._CONCEPT_ROOT_NODE.equalsIgnoreCase(edge.getSource())) {
                        isRootConnected = true;
                        break;
                    }
                }
                sb.append(isRootConnected);
                if (sb.length() > 0) {
                    out.print(sb.toString());
                    out.println();
                    sb.setLength(0);
                }
            }
        }
    }


        private void exportEscapedField(PrintWriter out, String field)
        {
            out.print(escapeDSV(field, delimiter));
        }

        /**
         * Escape a Delimiter-separated values string.
         *
         * @param input the input
         * @param delimiter the delimiter
         * @return the escaped output
         */
        public static String escapeDSV(String input, char delimiter)
        {
            char[] specialChars = new char[] { delimiter, DSV_QUOTE, DSV_LF, DSV_CR };

            boolean containsSpecial = false;
            for (int i = 0; i < specialChars.length; i++) {
                if (input.contains(String.valueOf(specialChars[i]))) {
                    containsSpecial = true;
                    break;
                }
            }

            if (containsSpecial) {
                return DSV_QUOTE_AS_STRING
                        + input.replaceAll(DSV_QUOTE_AS_STRING, DSV_QUOTE_AS_STRING + DSV_QUOTE_AS_STRING)
                        + DSV_QUOTE_AS_STRING;
            }

            return input;
        }

        private static final char DSV_QUOTE = '"';
        private static final char DSV_LF = '\n';
        private static final char DSV_CR = '\r';
        private static final String DSV_QUOTE_AS_STRING = String.valueOf(DSV_QUOTE);
    }