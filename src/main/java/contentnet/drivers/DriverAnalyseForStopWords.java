package contentnet.drivers;

import contentnet.GlobalProperties;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.nio.file.Files.*;

public class DriverAnalyseForStopWords {

    public static void main(String[] args) {
        DriverAnalyseForStopWords driver = new DriverAnalyseForStopWords();
        driver.doWork();
    }

    public void doWork() {

        Map<String, Integer> rawWordsCount = new HashMap<>();
        Map<String, Integer> removedWordsCount = new HashMap<>();

        String folderName = GlobalProperties.getSetting("result.processed.folder");
        String masterCopyFolderName = GlobalProperties.getSetting("result.processed_master_copies.folder");
        try (Stream<Path> paths = walk(Paths.get(folderName), 1)) {
            paths.filter(Files::isRegularFile).forEach(filePath -> {
                System.out.println("Processing file " + filePath);
                String fileNameString = filePath.getFileName().toString();
                String categoryName = fileNameString.substring(fileNameString.indexOf("_") + 1, fileNameString.indexOf("."));
                Graph<String, ConceptEdge> wordGraphProc = new DirectedMultigraph<>(ConceptEdge.class);
                GraphUtils.importGraphMatrix(wordGraphProc, filePath.toString());
                Graph<String, ConceptEdge> wordGraphRaw = new DirectedMultigraph<>(ConceptEdge.class);
                String rawFilePath = masterCopyFolderName + "\\wordGraph_" + categoryName + "_unproc.csv";
                System.out.println(rawFilePath);
                GraphUtils.importGraphMatrix(wordGraphRaw, rawFilePath);
                for (String rawWord : wordGraphRaw.vertexSet()) {
                    if (GraphUtils._CONCEPT_ROOT_NODE.equals(rawWord)) {
                        continue;
                    }

                    if (rawWordsCount.containsKey(rawWord)) {
                        rawWordsCount.put(rawWord, rawWordsCount.get(rawWord) + 1);
                    } else {
                        rawWordsCount.put(rawWord, 1);
                    }

                    boolean isWordStay = wordGraphProc.containsVertex(rawWord);
                    if (!isWordStay) {
                        if (removedWordsCount.containsKey(rawWord)) {
                            removedWordsCount.put(rawWord, removedWordsCount.get(rawWord) + 1);
                        } else {
                            removedWordsCount.put(rawWord, 1);
                        }
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        Map<String, List<String>> semMapping = GraphUtils.getSemanticMapping();
        //handle stop words
        analyseWords(rawWordsCount, removedWordsCount, semMapping, 5, 59, GlobalProperties.getSetting("data.semnatics.stop_words_dynamic"), true);
        //handle relaxed words
        analyseWords(rawWordsCount, removedWordsCount, semMapping, 3, 50,  GlobalProperties.getSetting("data.semnatics.relaxed_stop_words"), true);
    }

    private void analyseWords(Map<String, Integer> rawWordsCount, Map<String, Integer> removedWordsCount, Map<String, List<String>> semMapping, int minCount, int minRemoveProc, String file, boolean writeToFile) {
        PrintWriter w = null;
        if (writeToFile) {
            try {
                w = new PrintWriter(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        for (String rawWord : rawWordsCount.keySet()) {
            int rawCount = rawWordsCount.get(rawWord);
            int removedWordCount = removedWordsCount.get(rawWord) == null ? 0 : removedWordsCount.get(rawWord);
            if (rawCount >= minCount && (removedWordCount*100/rawCount) >= minRemoveProc && !semMapping.containsKey(rawWord)) {
                if (writeToFile)
                    w.println(rawWord);
                else
                    System.out.println("Possible stop word " + rawWord);
            }
        }
        if(writeToFile)
            w.close();
    }
}
