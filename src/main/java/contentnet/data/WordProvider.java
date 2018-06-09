package contentnet.data;

import contentnet.GlobalProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class WordProvider {

    public static void main(String[] args) {
        Set<String> words = WordProvider.getAllProductSemanticWords(0);
        for (String word : words) {
            System.out.println(word);
        }
    }

    public static Set<String> getAllProductSemanticWords(int firstNRecords) {

        Set<String> productSemanticWords = new HashSet<>();

        String fileName = GlobalProperties.getSetting("data.word_provider.file_name");
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            Stream<String> streamInWork = stream.skip(1);
            if(firstNRecords > 0)
                streamInWork = streamInWork.limit(firstNRecords);
            Consumer<String> fileLineConsumer = (lineString) -> {
                String[] splittedStrings = lineString.split(";");
                String productSemWord = splittedStrings[1].trim();
                productSemanticWords.add(productSemWord);
            };
            streamInWork.forEach(fileLineConsumer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return productSemanticWords;
    }
}
