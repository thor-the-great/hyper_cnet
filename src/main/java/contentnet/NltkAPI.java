package contentnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NltkAPI {

    private static NltkAPI instance;

    private final String _PATH_TO_PYTHON_INTERPRETER = "C:\\dev\\python\\anaconda\\envs\\wordnet_nltk\\python.exe";
    private final String _PATH_TO_SCRIPT_FOLDER = "C:\\work\\ariba\\wordnet_nltk";

    private NltkAPI() {
    }

    public static NltkAPI getInstance() {
        if (instance == null)
            instance = new NltkAPI();
        return instance;
    }

    List<String> doGenericCall(String scriptFileName, String ... args) {
        String[] scriptArgs = new String[args.length + 2];
        scriptArgs[0] = _PATH_TO_PYTHON_INTERPRETER;
        scriptArgs[1] = _PATH_TO_SCRIPT_FOLDER + File.separator + scriptFileName;
        for (int i = 0; i < args.length; i++) {
            scriptArgs[i + 2] = args[i];
        }
        List<String> result = new ArrayList<>();
        try {
            Process pythonScriptProcess = Runtime.getRuntime().exec(scriptArgs);
            BufferedReader br = new BufferedReader(new InputStreamReader(pythonScriptProcess.getInputStream()));
            String nextOutputLine;
            while ((nextOutputLine = br.readLine()) != null) {
                result.add(nextOutputLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<String> getLowestCommonHypernyms(String ... args) {
        return doGenericCall("wn_get_lowest_common_hypernyms.py", args);
    }

    public Set<String> getHypernymTree(String word) {
        List<String> hypernymLines = doGenericCall("wn_get_hypernyms_tree.py", word);
        //for now work with one liner
        if (hypernymLines.size() == 0)
            return new HashSet<>();
        String[] words = hypernymLines.get(0).split(",");
        Set<String> hypernymWords = new HashSet<>(Arrays.asList(words));
        return hypernymWords;
    }

    public float[] getHighestSimilarities(String ... words) {
        List<String> similarityString = doGenericCall("wn_get_highest_similarity.py", words);
        String[] similarities = similarityString.get(0).split(";");
        float[] result = new float[similarities.length];
        AtomicInteger count = new AtomicInteger(0);
        Arrays.stream(similarities).forEach( similarity -> result[count.getAndIncrement()] = Float.parseFloat(similarity));
        return result;
    }

    public float getHighestSimilarity(String ... words) {
        List<String> similarityString = doGenericCall("wn_get_highest_similarity.py", words);
        String[] similarities = similarityString.get(0).split(";");
        float hiestSimilarity = 0.0f;
        for (String nextSimilString : similarities){
            float nextSimil = Float.parseFloat(nextSimilString);
            if (nextSimil > hiestSimilarity)
                hiestSimilarity = nextSimil;
        }
        return hiestSimilarity;
    }
}
