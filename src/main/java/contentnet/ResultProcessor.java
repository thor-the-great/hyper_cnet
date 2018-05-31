package contentnet;

import contentnet.weightprocessing.IResultProcessingStrategy;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class ResultProcessor {
    static ResultProcessor instance;

    private ResultProcessor() {

    }

    public static ResultProcessor getInstance() {
        if (instance == null)
            instance = new ResultProcessor();
        return instance;
    }

    public List<String> sanitizeWordList(List<String> wordList, Set<String> ongoingProcessedWords) {
        for (int i = wordList.size() - 1; i >= 0; i--) {
            String word = wordList.get(i);
            //if ("device".equalsIgnoreCase(word))
            //    System.out.println("remove critical " + word);
            if (ongoingProcessedWords.contains(word)) {
                //System.out.println("Filtered out word " + word);
                wordList.remove(i);
            } else {
                ongoingProcessedWords.add(word);
            }
        }
        return wordList;
    }

    public Set<String> processHypernyms(JSONObject json, String word) {
        Set<String> result = new HashSet<>();
        JSONArray edges = json.getJSONArray("edges");
        // goes through the edges array
        for (int x = 0; x < edges.length(); x++) {
            JSONObject startObj = edges.getJSONObject(x).getJSONObject("end");
            if (startObj != null) {
                //String label = startObj.getString("term");
                String label = Utils.normalizeCNString(startObj.getString("label"));
                if (!label.equalsIgnoreCase(word))
                    result.add(label);
            }
        }
        return result;
    }

    public List<String> processRelatedTo(JSONObject json, String word) {
        List<String> result = new ArrayList<>();
        JSONArray edges = json.getJSONArray("edges");
        // goes through the edges array
        for (int x = 0; x < edges.length(); x++) {
            JSONObject startObj = edges.getJSONObject(x).getJSONObject("end");
            if (startObj != null) {
                //String label = startObj.getString("term");
                String label = Utils.normalizeCNString(startObj.getString("label"));
                if (!label.equalsIgnoreCase(word))
                    result.add(label);
            }
        }
        return result;
    }

    public float processRelationWeight(JSONObject json) {
        JSONArray related = json.getJSONArray("related");

        if (related.length() == 0) {
            return 0.0f;
        }
        return related.getJSONObject(0).getFloat("weight");
    }

    public Map<String, Float> processWordsWeights(String mainWord, Map<String, Float> rawResults, IResultProcessingStrategy processingCommand, boolean isPrint) {
        rawResults = processingCommand.processRelationWeights(rawResults);
        if (isPrint) {
            for (String key : rawResults.keySet()) {
                System.out.println(mainWord + "->" + Utils.getLabelFromConceptContextName(key) + ": " + rawResults.get(key));
            }
        }
        return rawResults; 
    }

    public Set<String> adjustWordsPerWeights(Set<String> words, Map<String, Float> weightResults) {
        /*for (int i = words.size() - 1; i >= 0; i--) {
            String word = words.get(i);
            if (!weightResults.containsKey(word)) {
                words.remove(i);
            }
        }*/
        for ( Iterator<String> it = words.iterator(); it.hasNext(); ) {
            String word = it.next();
            if (!weightResults.containsKey(word)) {
                it.remove();
            }
        }
        return words;
    }
}
