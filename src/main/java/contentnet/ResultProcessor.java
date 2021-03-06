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

    public boolean isResultEmptyEdgesEnds(JSONObject json) {
        //boolean result = true;
        //Set<String> result = new HashSet<>();
        /*JSONArray edges = json.getJSONArray("edges");
        // goes through the edges array
        for (int x = 0; x < edges.length(); x++) {
            JSONObject startObj = edges.getJSONObject(x).getJSONObject("end");
            if (startObj != null) {
                return false;
            }
        }*/
        boolean result = json.getJSONArray("edges").length() == 0;
        return result;
    }

    public Set<String> extractEdgeEnds(JSONObject json, String word) {
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

    public boolean isEdgeEndsInList(JSONObject json, String word, List<String> words) {
        boolean result = false;
        //Set<String> result = new HashSet<>();
        JSONArray edges = json.getJSONArray("edges");
        // goes through the edges array
        for (int x = 0; x < edges.length(); x++) {
            JSONObject startObj = edges.getJSONObject(x).getJSONObject("end");
            if (startObj != null) {
                //String label = startObj.getString("term");
                String label = Utils.normalizeCNString(startObj.getString("label"));
                if (!label.equalsIgnoreCase(word) && words.contains(label))
                    //result.add(label);
                    return true;
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
        for ( Iterator<String> it = words.iterator(); it.hasNext(); ) {
            String word = it.next();
            if (!weightResults.containsKey(word)) {
                it.remove();
            }
        }
        return words;
    }

    public Map<String, Float> getRelationWeight(String mainWord, Set<String> wordsInQuestion) {
        Map<String, Float> wordsWeight = new HashMap();
        int count = 0;
        for (String wordInQuestion: wordsInQuestion) {
            count++;
            //ugly workaround due to bug in conceptnet - it can't process correctly cases when we do filter by the complex word
            //(more than 2 words). So it has to be on the first position in query. Result has transitive property so it will
            //remain the same
            boolean isMainWordComplex = mainWord.contains("_");
            boolean isWordInQuestionComplex = wordInQuestion.contains("_");
            float weightResult = 0.0f;
            if ((!isMainWordComplex && !isWordInQuestionComplex) || (isMainWordComplex && !isWordInQuestionComplex)) {
                weightResult = ResultProcessor.getInstance().processRelationWeight(
                        ConceptnetAPI.getInstance().getRelationWeight(mainWord, Utils.getLabelFromConceptContextName(wordInQuestion)));
            } else {
                weightResult = ResultProcessor.getInstance().processRelationWeight(
                        ConceptnetAPI.getInstance().getRelationWeight(Utils.getLabelFromConceptContextName(wordInQuestion), mainWord));
            }
            wordsWeight.put(wordInQuestion, weightResult);
            if (count % 100 == 0)
                System.out.println("Processed relation for " + count + " words");
            //Utils.doDelay(Utils.DELAY);
        }
        return wordsWeight;
    }
}
