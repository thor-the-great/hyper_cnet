package contentnet;

import contentnet.graph.GraphUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConceptnetAPI {

    public static final Set<String> ALLOWED_CONTEXT = new HashSet<String>() {
        {
            /*add("computing");
            add("nautical");
            add("chemistry");
            add("medicine");
            add("sports");
            add("architecture");
            add("electronics");
            add("physics");
            add("engineering");
            add("video_games");
            add("printing");
            add("geology");
            add("organic_compound");
            add("manufacturing");
            add("printing");
            add("material");
            add("unit");
            add("automotive");
            add("biochemistry");
            add("firearms");
            add("textiles");
            add("television");*/

            //add("unix");
            //add("microsoft_windows");
            add("computer");
            add("computer_hardware");
            add("software");
            add("computing");
        }
    };

    static ResultProcessor resultProcessor = ResultProcessor.getInstance();

    static ConceptnetAPI instance;

    static Map<String, HttpURLConnection> httpConnectionCache = new HashMap<>();

    private ConceptnetAPI() {}

    public static ConceptnetAPI getInstance() {
        if (instance == null)
            instance = new ConceptnetAPI();
        return instance;
    }

    public JSONObject genericCall(String apiString) {
        JSONObject result = null;
        try {
            // open HttpURLConnection
            HttpURLConnection hp = (HttpURLConnection) new URL(apiString).openConnection();
            hp.setRequestProperty("Connection", "Keep-Alive");
            hp.setRequestProperty("Keep-Alive", "header");
            hp.setRequestProperty("Content-Length", "0");
            //hp.setUseCaches(true);
            //hp.setRequestProperty("Connection", "close");
            // set to request method to get
            // not required since default
            hp.setRequestMethod("GET");
            // get the inputstream in the json format
            hp.setRequestProperty("Accept", "application/json");
            //hp.setRequestProperty("Content-Length", Integer.toString(hp.getContentLength()));
            // get inputstream from httpurlconnection
            InputStream is = hp.getInputStream();
            //InputStream is = getConnection(apiString);
            // get text from inputstream using IOUtils
            String jsonText = IOUtils.toString(is, Charset.forName("UTF-8"));
            // get json object from the json String
            result = new JSONObject(jsonText);
            //is.close();

            if (Utils.DELAY > 0 ) {
                Thread.sleep(Utils.DELAY);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject getHyponyms(String hyponym) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/IsA&end=/c/en/{1}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, hyponym);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getIsA(String hypernym) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/IsA&start=/c/en/{1}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, hypernym);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getIsAForTwoWords(String hyponym, String hypernym) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/IsA&start=/c/en/{2}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, hyponym, hypernym);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getRelationWeight(String mainWord, String wordInQuestion) {
        String apiTemplate = "http://{0}/related/c/en/{1}?filter=/c/en/{2}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, mainWord, wordInQuestion);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getRelatedTo(String mainWord) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/RelatedTo&start=/c/en/{1}&end=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, mainWord);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getWordContext(String mainWord) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/HasContext&start=/c/en/{1}&end=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, mainWord);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getHasA(String base, String part) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/HasA&end=/c/en/{2}";
        //String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/HasContext&start=/c/en/{1}&end=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, base, part);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getAtLocation(String base, String part) {
        String apiTemplate = "http://{0}/query?node=/c/en/{2}&rel=/r/AtLocation&end=/c/en/{1}";
        //String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/HasContext&start=/c/en/{1}&end=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, base, part);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public boolean isValidWord(String word) {
        String apiTemplate = "http://{0}/c/en/{1}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, word);
        JSONObject result = genericCall(apiCall);
        //return resultProcessor.isResultEmptyEdgesEnds(result);
        return (result.getJSONArray("edges").length() > 0);
    }

    public Set<String> getWordRoots(String mainWord) {
        //String apiTemplate = "http://{0}/c/en/{1}?rel=/r/FormOf"
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/FormOf&start=/c/en/{1}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, mainWord);
        JSONObject result = genericCall(apiCall);
        Set<String> wordRoots = extractEdgeEnds(result, mainWord);
        if (wordRoots.contains(mainWord))
            wordRoots.remove(mainWord);
        return wordRoots;
    }

    public Set<String> getSynonyms(String mainWord) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/Synonym&start=/c/en/{1}&other=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, mainWord);
        JSONObject result = genericCall(apiCall);
        Set<String> synonyms = extractEdgeEnds(result, mainWord);
        return synonyms;
    }

    private Set<String> extractEdgeEnds(JSONObject json, String word) {
        return extractEdgeByTag(json, word, "end");
    }

    private Set<String> extractEdgeByTag(JSONObject json, String word, String tag) {
        Set<String> result = new HashSet<>();
        JSONArray edges = json.getJSONArray("edges");
        // goes through the edges array
        for (int x = 0; x < edges.length(); x++) {
            JSONObject startObj = edges.getJSONObject(x).getJSONObject(tag);
            if (startObj != null) {
                //String label = startObj.getString("term");
                String label = Utils.normalizeCNString(startObj.getString("label"));
                if (!label.equalsIgnoreCase(word))
                    result.add(label);
            }
        }
        return result;
    }

    public Set<String> getWordForms(String word) {
        String apiTemplate = "http://{0}/query?node=/c/en/{1}&rel=/r/FormOf&end=/c/en/{1}";
        String apiCall = MessageFormat.format(apiTemplate, GlobalProperties._SERVER_PORT, word);
        JSONObject result = genericCall(apiCall);
        Set<String> wordFormsStart = extractEdgeByTag(result, word, "start");
        return wordFormsStart;
    }
}
