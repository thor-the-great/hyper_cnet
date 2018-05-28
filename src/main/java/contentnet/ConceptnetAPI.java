package contentnet;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

public class ConceptnetAPI {

    public static final Set<String> ALLOWED_CONTEXT = new HashSet<String>() {
        {
            add("computing");
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
        }
    };

    static ConceptnetAPI instance;

    private ConceptnetAPI() {

    }

    public static ConceptnetAPI getInstance() {
        if (instance == null)
            instance = new ConceptnetAPI();
        return instance;
    }

    public JSONObject genericCall(String apiString) {
        JSONObject result = null;
        try {
            // open HttpURLConnection
            HttpURLConnection hp = (HttpURLConnection) new URL(apiString)
                    .openConnection();
            // set to request method to get
            // not required since default
            hp.setRequestMethod("GET");
            // get the inputstream in the json format
            hp.setRequestProperty("Accept", "application/json");
            // get inputstream from httpurlconnection
            InputStream is = hp.getInputStream();
            // get text from inputstream using IOUtils
            String jsonText = IOUtils.toString(is, Charset.forName("UTF-8"));
            // get json object from the json String
            result = new JSONObject(jsonText);
            is.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public JSONObject getHyponyms(String hyponym) {
        String apiTemplate = "http://api.conceptnet.io/query?node=/c/en/{0}&rel=/r/IsA&end=/c/en/{0}";
        String apiCall = MessageFormat.format(apiTemplate, hyponym);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getHypernyms(String hypernym) {
        String apiTemplate = "http://api.conceptnet.io/query?node=/c/en/{0}&rel=/r/IsA&start=/c/en/{0}";
        String apiCall = MessageFormat.format(apiTemplate, hypernym);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getRelationWeight(String mainWord, String wordInQuestion) {
        String apiTemplate = "http://api.conceptnet.io/related/c/en/{0}?filter=/c/en/{1}";
        String apiCall = MessageFormat.format(apiTemplate, mainWord, wordInQuestion);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getRelatedTo(String mainWord) {
        String apiTemplate = "http://api.conceptnet.io/query?node=/c/en/{0}&rel=/r/RelatedTo&start=/c/en/{0}&end=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, mainWord);
        JSONObject result = genericCall(apiCall);
        return result;
    }

    public JSONObject getHasContext(String mainWord) {
        String apiTemplate = "http://api.conceptnet.io/query?node=/c/en/{0}&rel=/r/HasContext&start=/c/en/{0}&end=/c/en";
        String apiCall = MessageFormat.format(apiTemplate, mainWord);
        JSONObject result = genericCall(apiCall);
        return result;
    }
}
