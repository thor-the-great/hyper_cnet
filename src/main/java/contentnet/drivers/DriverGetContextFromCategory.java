package contentnet.drivers;

import contentnet.ResultProcessor;
import contentnet.category.UNSPSCRecord;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DriverGetContextFromCategory {

    public static void main(String[] args) {
        DriverGetContextFromCategory driver = new DriverGetContextFromCategory();
        driver.doWork();
    }

    public Map<String, UNSPSCRecord> doWork() {

        Map<String, Set<String>> contextCache = new HashMap();

        String fileName = "C:\\work\\ariba\\unique_context.csv";
        Set<String> allContexts = new HashSet<>();
        try (Stream<String> stream = Files.lines(Paths.get(fileName))) {
            Consumer<String> fileLineConsumer = (lineString) -> {
                String wordString = lineString.substring(0, lineString.indexOf(',')).trim();
                allContexts.add(wordString);
                //System.out.println(wordString);
            };
            stream.forEach(fileLineConsumer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        /*String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43212001_0613_1613.txt";
        BufferedReader br;
        UNSPSCRecord category = new UNSPSCRecord();
        try {
            br = new BufferedReader(new FileReader(fileName2));
            String categoryValues = br.readLine().split(":")[1];
            int firstSeparatorIndex = categoryValues.indexOf(",");
            String categoryCode = categoryValues.substring(0, firstSeparatorIndex);
            String categoryCodeDescription = categoryValues.substring(firstSeparatorIndex + 1);
            category = new UNSPSCRecord();
            category.setUnspsc(categoryCode);
            category.setUnspscName(categoryCodeDescription);
        } catch (IOException e) {
            e.printStackTrace();
        }*/

        String categoryToProcess = "43211804";

        String fileName2 = "C:\\work\\ariba\\unspsc_semantics_category_4321_test.txt";
            Map<String, UNSPSCRecord> categorySemantics = new HashMap<>();

            try (Stream<String> stream = Files.lines(Paths.get(fileName2))) {
                Stream<String> streamSkipped = stream.skip(1);
            Consumer<String> fileLineConsumer = (lineString) -> {
                String[] splittedStrings = lineString.split(";");
                String unspsc = splittedStrings[1].trim();
                String unspscName = splittedStrings[4].trim();
                String semType = splittedStrings[2].trim();
                String semWord = splittedStrings[3].trim();

                if (unspsc.equals(categoryToProcess)) {
                    if (!categorySemantics.containsKey(unspsc)) {
                        UNSPSCRecord record = new UNSPSCRecord();
                        record.setUnspsc(unspsc);
                        record.setUnspscName(unspscName);
                        List<String> attrList = new ArrayList<>();
                        attrList.add(semWord);
                        record.getAttributes().put(semType, attrList);
                        categorySemantics.put(unspsc, record);
                    } else {
                        UNSPSCRecord record = categorySemantics.get(unspsc);
                        Map<String, List<String>> attr = record.getAttributes();
                        if (attr.containsKey(semType)) {
                            attr.get(semType).add(semWord);
                        } else {
                            List<String> attrList = new ArrayList<>();
                            attrList.add(semWord);
                            attr.put(semType, attrList);
                        }
                    }
                }
            };
            streamSkipped.forEach(fileLineConsumer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        for (String categorySemantic: categorySemantics.keySet()) {

            UNSPSCRecord record = categorySemantics.get(categorySemantic);

            List<String> productAttributes = record.getAttributes().get(UNSPSCRecord._ATTR_NAME_PRODUCT);
            List<String> genericAttributes = record.getAttributes().get(UNSPSCRecord._ATTR_NAME_ATTRVALUE);
            List<String> joinedAttrList = new ArrayList<>();
            if (productAttributes != null)
                joinedAttrList.addAll(productAttributes);
            if (genericAttributes != null)
                joinedAttrList.addAll(genericAttributes);

            for (String attribute: joinedAttrList) {
                if (contextCache.containsKey(attribute)) {
                    record.getCategoryContext().addAll(contextCache.get(attribute));
                    System.out.println("Skipped 1 " + attribute);
                    continue;
                }
                Map<String, Float> relationWeights = ResultProcessor.getInstance().getRelationWeight(attribute, allContexts);
                Set<String> contextOfOneAttribute = new HashSet<>();
                for (String word : relationWeights.keySet()) {
                    float relationWeight = relationWeights.get(word);
                    if (relationWeight > 0.2) {
                        System.out.println(word + " context, weight " + relationWeights.get(word) + " for category " + record.getUnspscName());
                        //record.getCategoryContext().add(word);
                        contextOfOneAttribute.add(word);
                    }
                }
                record.getCategoryContext().addAll(contextOfOneAttribute);
                if (!contextCache.containsKey(attribute)) {
                    contextCache.put(attribute, contextOfOneAttribute);
                }
            }
        }


        /*for (String categorySemantic: categorySemantics.keySet()) {
            Map<String, Float> relationWeights = ResultProcessor.getInstance().getRelationWeight(categorySemantic, allContexts);
            for (String word: relationWeights.keySet()) {
                float relationWeight = relationWeights.get(word);
                if (relationWeight > 0.4) {
                    System.out.print(word + " context with weight " + relationWeights.get(word) + " for category semantics "+ categorySemantic + " for categories : ");
                    for (String unspscName : categorySemantics.get(categorySemantic) ) {
                        System.out.print("'" + unspscName + "', ");
                    }
                    System.out.print("\n");
                }
            }
        }*/
        return categorySemantics;
    }
}
