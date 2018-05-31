package contentnet.drivers;

import contentnet.ConceptnetAPI;
import contentnet.ResultProcessor;
import contentnet.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DriverContextStatistics {

    public static void main(String[] args) {
//        String fileName = args[0];
//        Set<String> allWords = new HashSet<>();
//        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
//            Stream<String> streamSkipped = stream.skip(1);
//
//            Consumer<String> fileLineConsumer = (lineString) -> {
//                String wordString = lineString.substring(lineString.indexOf(';') + 1, lineString.lastIndexOf(';')).trim();
//                allWords.add(wordString);
//                //System.out.println(wordString);
//            };
//
//            streamSkipped.forEach(fileLineConsumer);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//
//
//        Set<String> wordsWithNoContext = new HashSet<>();
//        Map<String, Integer> contexts = new HashMap<>();
//
//        int setSize = 0;
//
//        for (String word : allWords) {
//            if (setSize >= 4000) {
//                break;
//            } else if (setSize % 25 == 0){
//                System.out.println(">>>" + setSize);
//            }
//            setSize++;
//            List<String> hasContextResuts =
//                    ResultProcessor.getInstance().processHypernyms(
//                            ConceptnetAPI.getInstance().getHasContext(word), word);
//            if (hasContextResuts.size() == 0) {
//                //System.out.println("Word '" + word + "' has no context");
//                wordsWithNoContext.add(word);
//                continue;
//            }
//            for (int i = hasContextResuts.size() - 1; i>=0; i--) {
//                String oneContext = hasContextResuts.get(i);
//                if (contexts.containsKey(oneContext))
//                    contexts.put(oneContext, contexts.get(oneContext) + 1);
//                else {
//                    contexts.put(oneContext, 1);
//                }
//            }
//           /* for (int i = hasContextResuts.size() - 1; i>=0; i--) {
//                if (!ConceptnetAPI.ALLOWED_CONTEXT.contains(hasContextResuts.get(i))) {
//                    hasContextResuts.remove(i);
//                }
//            }*/
//            /*for (String contextOf: hasContextResuts) {
//                System.out.println("Context: " + word + " => " + contextOf);
//            }*/
//
//            Utils.doDelay(150);
//        }
//
//        System.out.println("Different contexts : " + contexts.size() );
//        System.out.println("Words without any contexts : " + wordsWithNoContext.size() );
//
//        Path path = Paths.get("C:\\work\\ariba\\word_without_context.txt");
//        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
//            for (String word: wordsWithNoContext) {
//                writer.write(word + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        path = Paths.get("C:\\work\\ariba\\unique_context.txt");
//        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
//            for (String context: contexts.keySet()) {
//                writer.write(context + "," + contexts.get(context) + "\n");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
