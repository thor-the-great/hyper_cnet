package contentnet.graph;

import org.jgrapht.*;
import org.jgrapht.io.CSVFormat;
import org.jgrapht.io.ExportException;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConceptGraphCSVExport  {
        private static final char DEFAULT_DELIMITER = ',';

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
            format = CSVFormat.ADJACENCY_LIST;
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
                //case MATRIX:
                //    exportAsMatrix(g, out);
                //    break;
            }
            out.flush();
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
            for (String v : g.vertexSet()) {
                //exportEscapedField(out, vertexIDProvider.getName(v));
                exportEscapedField(out, v);
                Set<ConceptEdge> edges = new HashSet<>();
                for (ConceptEdge e : g.outgoingEdgesOf(v)) {
                    String w = Graphs.getOppositeVertex(g, e, v);
                    out.print(delimiter);
                    //exportEscapedField(out, vertexIDProvider.getName(w));
                    exportEscapedField(out, w);
                    edges.add(e);
                }
                out.println();
                for (ConceptEdge e : edges) {
                    out.print(e.getRelationType());
                    out.print(":");
                    Map<String, Object> attributes = e.getAttributes();
                    int attrCount = 0;
                    for (String attrKey : attributes.keySet()) {
                        out.print(attrKey);
                        out.print("=");
                        out.print(attributes.get(attrKey));
                        attrCount++;
                        if (attrCount < attributes.size())
                            out.print(":");
                    }
                    out.print(delimiter);
                }
                if (edges.size() > 0)
                    out.println();
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