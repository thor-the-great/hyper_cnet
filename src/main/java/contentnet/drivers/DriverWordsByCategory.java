package contentnet.drivers;

import contentnet.Utils;
import contentnet.category.UNSPSCRecord;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphAPI;
import contentnet.graph.GraphUtils;
import contentnet.processinglogic.IStrategy;
import contentnet.processinglogic.ProductStrategy;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.io.CSVFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Build h/s graph for provided set of words (product semantics) as per provided context and build edges for provided
 * categories
 */
public class DriverWordsByCategory {

    static final int MAX_RECUSRSION_LEVEL = 5;
    //static final int DELAY = Utils.DELAY;

    static final  boolean IS_LOG_ENABLED = false;

    static final String[] WORDS_SET2 = new String[]{
            //"case", "mount"
            //"mount", "tripod", "microphone"
            //"ipad"
            //"background", "notebook", "server", "interface", "string", "keyboard", "monitor", "player", "cap",
            //"connector", "dvd", "cover", "software"
            "lcd", "shade", "shades"
    };

    //static final String[] words = WORDS_SET100;
    //static String[] words = WORDS_SET_COMP_DISPLAY;
    //static final String[] words = WORDS_SET_COMP_INPUT_DEVICE;
    //static final String[] words = WORDS_SET100;
    //static final String[] words = WORDS_SET200;
    static String[] words = WORDS_SET2;
    //static final String[] words = WordProvider.getAllProductSemanticWords(250).toArray(new String[0]);
    public static final Set<String> UNSPSC_CATEGORY_CONTEXT = new HashSet<String>() {
        {
            //add("unix");
            //add("microsoft_windows");

            /*add("gui");
            add("graphical_user_interface");
            add("gaming");
            add("computer");
            add("computer_hardware");
            add("hardware");
            add("electronics");
            add("technology");
            add("computer_graphics");
            add("computing");*/

            /*add("computer");
            add("computer_hardware");
            add("hardware");
            add("electronics");
            add("technology");
            add("retail");
            add("entertainment");
            add("fashion");
            add("retailing");
            add("computer_technology");
            add("data_communications");
            add("computer_keyboards");
            add("electronics");
            add("peripheral");
            add("electronic_equipment");

            //add("computer_science");
            //add("mail");
            //add("computer_switch_box");
            /*add("typewriters");
            add("machine");
            add("typesetting");
            add("paper");*/

            //Computer display glare screens
            /*add("movie");
            add("gui");
            add("television");
            add("cinema_or_theater_special_effects");
            add("cinematography");
            add("computer");
            add("cinema");
            add("motion_pictures");
            add("film");
            add("theatre");
            add("tv");
            add("film_production");
            add("electronics");
            add("computer_graphics");*/

            //Computer data input devices
            /*add("computer");
            add("electronic_design_automation");
            add("unit");
            add("electronics");
            add("computer_hardware");
            add("computer_technology");
            add("mobile_phones");
            add("gaming");
            add("computer_keyboards");
            add("electrical_engineering");
            add("audio");*/

            //Tablet computers
            /*add("computer");
            add("unit");
            add("electronics");
            add("computer_hardware");
            add("computer_technology");
            add("mobile");
            add("gaming");
            add("computer_engineering");
            add("consumer_electronics");
            add("personal_electronics");
            add("personal_computer");*/

            //Computer mouse or trackballs
            /*add("computer");
            add("gaming");
            add("gui");
            add("graphical_user_interface");
            add("pinball");
            add("electronics");
            add("input_device");*/

            //keyboards
            /*add("computer");
            add("typing");
            add("computer_hardware");
            add("microsoft_windows");
            add("typewriters");
            add("operating_systems");
            add("computer_accessory");
            add("keyboard");
            add("input_device");*/

            //scanners
            /*add("computer");
            add("printing");
            add("photo");
            add("scanner");
            add("copy");
            add("copying");
            add("software");*/

            //encoder decoder equipment
            /*add("technical");
            add("audio_processing");
            add("broadcasting");
            add("sound_recording");
            add("electronics");
            add("cryptography");
            add("video_production");
            add("television");
            add("signal_processing");
            add("audio");
            add("tv");*/

            //usb hubs or connectors
            /*add("electronic_design");
            add("electronic");
            add("electrical_engineering");
            add("computer_hardware");
            add("networking");
            add("bridge");
            add("digital_communications");
            add("extension");
            add("computer_engineering");
            add("broadcast_medium");
            add("usb");*/

            //Peripheral switch boxes
            /*add("metal_type");
            add("peripheral");
            add("electrical_engineering");
            add("networking");
            add("computer_keyboards");
            add("computer_engineering");
            add("network");
            add("signalling");
            add("electrical");
            add("data_communications");
            add("communication");
            add("computer");*/

            //game pads and joysticks
            /*add("video_games");
            add("games");
            add("gaming");
            add("game_of_go");
            add("gui");
            add("computer_games");
            add("toys");
            add("game");
            add("mechanical");
            add("piloting");
            add("sport");*/

            //Computers
            /*add("computability_theory");
            add("computer_security");
            add("scientific_programming");
            add("mac_os_x");
            add("unix");
            add("computer_keyboards");
            add("computer_program");
            add("windows");
            add("computer");
            add("computer_hardware");
            add("electronics");
            add("computer_engineering");
            add("operating_systems");
            add("computer_technology");
            add("microsoft_windows");
            add("data_communications");
            add("computer_software");
            add("computer_graphics");
            add("computer_networking");
            add("computing");*/

            //computer printers
            /*add("printing");
            add("phototypesetting_and_digital_typesetting");
            add("newspapers");
            add("dyeing");
            add("typewriters");
            add("color");
            add("colour");
            add("typography");
            add("computer");
            add("computer_hardware");
            add("publishing");
            add("typesetting");
            add("paper");*/

            //docking stations
            /*add("computing");
            add("computer_hardware");
            add("peripheral");
            add("hardware");
            add("dock");
            add("computer_games");
            add("gaming");*/

            //computer printers
            /*add("printing");
            add("phototypesetting_and_digital_typesetting");
            add("newspapers");
            add("dyeing");
            add("typewriters");
            add("color");
            add("colour");
            add("typography");
            add("publishing");
            add("typesetting");
            add("paper");*/

            //compact disk labeling printer
            /*add("recording");
            add("optical_disk");
            add("printing");
            add("color");
            add("paper");
            add("disk");
            add("storage_device");
            add("marker");
            add("colour");
            add("publishing");
            add("computer_hardware");
            add("peripheral");*/

            //computer accessories
            /*add("accessory");
            add("retail");
            add("mac_os_x");
            add("fashion");
            add("jewelry");
            add("retailing");
            add("tools");
            add("computer");
            add("computer_hardware");
            add("electronics");
            add("computer_engineering");
            add("computer_technology");
            add("computing");*/

            //computers 432115
            /*add("computer");
            add("computer_hardware");
            add("hardware");
            add("electronics");
            add("technology");
            add("computer_technology");
            add("data_communications");
            add("electronic_equipment");
            add("server");
            add("unix");
            add("computing");*/

            //43212103,Dye sublimination printers
            /*add("dyeing");
            add("printing");
            add("color");
            add("colour");
            add("paper");
            add("typography");
            add("publishing");
            add("computer_hardware");
            add("peripheral");
            add("machine");*/

            //432118 Computer data input device accessories
            /*add("computer");
            add("computer_hardware");
            add("hardware");
            add("electronics");
            add("technology");
            add("computer_technology");
            add("data_communications");
            add("electronic_equipment");
            add("retail");
            add("fashion");
            add("jewelry");
            add("retailing");
            add("tools");
            add("audio");
            add("mobile_phones");
            add("entertainment");*/

            //432117,Computer data input devices
            /*add("computer");
            add("electronic_design_automation");
            add("unit");
            add("electronics");
            add("computer_hardware");
            add("computer_technology");
            add("gaming");
            add("computer_keyboards");
            add("electrical_engineering");
            add("audio");*/

            //43211511,Wearable computing devices
            /*add("computer");
            add("unit");
            add("electronics");
            add("computer_technology");
            add("gaming");
            add("audio");
            add("medical");
            add("vehicle");
            add("mobile_phones");
            add("photography");
            add("optics");
            add("drones");
            add("smart");
            add("wear");
            add("mobile");*/

            //43211501,Computer servers
            /*
            add("irc");
            add("cryptography");
            add("computer_security");
            add("unix");
            add("computer");
            add("computer_hardware");
            add("database");
            add("databases");
            add("operating_systems");
            add("networking");
            add("bsd");
            add("file_sharing");
            add("internet");
            add("computer_networking");
            add("server");*/

            //43211607, Computer speaker
            /*add("audio_processing");
            add("sound_recording");
            add("sound_engineering");
            add("acoustics");
            add("radio");
            add("audio_effects");
            add("audio");
            add("sound");
            add("computer");
            add("electronics");
            add("computer_technology");
            add("peripheral");*/

            //432119,Computer displays
            /*add("contrast");
            add("gui");
            add("movie");
            add("colors");
            add("art");
            add("graphical_user_interface");
            add("entertainment_industry");
            add("computer");
            add("motion_pictures");
            add("tv");
            add("electronics");
            add("computer_graphics");
            add("peripheral");*/

            //43211508,Personal computers,47
            /*add("computer");
            add("computer_hardware");
            add("internet");
            add("gaming");
            add("mac_os_x");
            add("electronics");
            add("business");
            add("personal");
            add("private");
            add("portable");*/

            //432120,Computer display accessories,32
            /*add("contrast");
            add("gui");
            add("movie");
            add("colors");
            add("art");
            add("graphical_user_interface");
            add("entertainment_industry");
            add("computer");
            add("motion_pictures");
            add("tv");
            add("electronics");
            add("computer_graphics");
            add("peripheral");
            add("tools");
            add("retail");
            add("entertainment");
            add("retailing");*/

            //43211712,Graphics tablets
            /*add("gui");
            add("computer");
            add("graphical_user_interface");
            add("computer_hardware");
            add("computing");
            add("mobile");
            add("portable");
            add("computer_graphics");
            add("pictures");
            add("audio");
            add("manga");
            add("colors");
            add("animation");
            add("printing");
            add("visual_art");*/

            //43211506,Thin client computers
            /*add("computer");
            add("computer_hardware");
            add("internet");
            add("electronics");
            add("business");
            add("personal");
            add("portable");
            add("technology");
            add("client");
            add("computer_graphics");*/

            //43212105,Laser printers
            /*add("printing");
            add("newspapers");
            add("dyeing");
            add("color");
            add("colour");
            add("typography");
            add("publishing");
            add("typesetting");
            add("paper");
            add("optics");*/

            //43211902,Liquid crystal display LCD panels or monitors
            /*add("graphical_user_interface");
            add("computer_hardware");
            add("tv");
            add("contrast");
            add("colors");
            add("contrast_streak");
            add("entertainment_industry");
            add("crystallography");
            add("mineral");
            add("television");
            add("electronics");*/

            //43211804,Keyboard drawers or shelves
            add("mac_os_x");
            add("tools");
            add("furniture");
            add("storage");
            add("interior_decorating");
            add("gaming");
            add("typewriters");
            add("computer_hardware");

        }
    };
    IStrategy processingStrategy = new ProductStrategy(IS_LOG_ENABLED);
    //IStrategy processingStrategy = new SmallProductStrategy(IS_LOG_ENABLED);
    //IStrategy processingStrategy = new GeneralStrategy(IS_LOG_ENABLED);

    public static void main(String[] args) {

        String categoryToProcess = "43211804";

        String folderName = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data";
        String fileNames[] = new String[1];

        try {
            Stream<Path> path = Files.walk(Paths.get(folderName), 1);
            path.filter(Files::isRegularFile).
                    filter(filePath -> filePath.getFileName().toString().endsWith(".txt") && filePath.getFileName().toString().startsWith("hwByCategory_") && filePath.getFileName().toString().split("_")[1].equals(categoryToProcess)).
                    forEach(filePath-> {
                        fileNames[0] = filePath.toString();
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        String fileName2 = fileNames[0];

        BufferedReader br;
        final UNSPSCRecord category = new UNSPSCRecord();;
        try {
            br = new BufferedReader(new FileReader(fileName2));
            String categoryValues = br.readLine().split(":")[1];
            int firstSeparatorIndex = categoryValues.indexOf(",");
            String categoryCode = categoryValues.substring(0, firstSeparatorIndex);
            String categoryCodeDescription = categoryValues.substring(firstSeparatorIndex + 1);

            category.setUnspsc(categoryCode);
            category.setUnspscName(categoryCodeDescription);

            String wordsLine = br.readLine().split(":")[1];
            String[] wordsArray = wordsLine.split(",");
            words = wordsArray;

        } catch (IOException e) {
            e.printStackTrace();
        }

        DriverWordsByCategory driver = new DriverWordsByCategory();

        String categoriesBase = "C:\\work\\ariba\\unspsc_semantics_category_4321_master_copy.txt";

        try (Stream<String> stream = Files.lines(Paths.get(categoriesBase))) {
            Stream<String> streamSkipped = stream.skip(1);
            Consumer<String> fileLineConsumer = (lineString) -> {
                String[] splittedStrings = lineString.split(";");
                String unspsc = splittedStrings[1].trim();
                String unspscName = splittedStrings[4].trim();
                String semType = splittedStrings[2].trim();
                String semWord = splittedStrings[3].trim();

                if (category.getUnspsc().equalsIgnoreCase(unspsc)) {
                    if("product".equalsIgnoreCase(semType)) {
                        if (!category.getAttributes().containsKey(UNSPSCRecord._ATTR_NAME_PRODUCT)) {
                            category.getAttributes().put(UNSPSCRecord._ATTR_NAME_PRODUCT, new ArrayList<>());
                        }
                        category.getAttributes().get(UNSPSCRecord._ATTR_NAME_PRODUCT).add(semWord);
                    } else {
                        if (!category.getAttributes().containsKey(UNSPSCRecord._ATTR_NAME_ATTRVALUE)) {
                            category.getAttributes().put(UNSPSCRecord._ATTR_NAME_ATTRVALUE, new ArrayList<>());
                        }
                        category.getAttributes().get(UNSPSCRecord._ATTR_NAME_ATTRVALUE).add(semWord);
                    }
                }
            };
            streamSkipped.forEach(fileLineConsumer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        driver.enrichCategoryAttributes(category);

        driver.doWork(words, UNSPSC_CATEGORY_CONTEXT, category);
    }

    Graph<String, ConceptEdge> doWork(String[] words, Set<String> categoryContext, UNSPSCRecord categoryRecord) {
        return doWork(words, categoryContext, categoryRecord, null);
    }


    public Graph<String, ConceptEdge> doWork(String[] words, Set<String> categoryContext, UNSPSCRecord categoryRecord, Graph<String, ConceptEdge> wordGraph) {
        long startTime = System.currentTimeMillis();

        if (wordGraph == null)
            wordGraph = new DirectedMultigraph<>(ConceptEdge.class);
        //GraphUtils.importGraph(wordGraph, GraphUtils._INWORK_2_CSV_FILE_PATH);
        if (!wordGraph.containsVertex(GraphUtils._CONCEPT_ROOT_NODE)) {
            wordGraph.addVertex(GraphUtils._CONCEPT_ROOT_NODE);
        }
        System.out.println("Processing words: " + words.length);
        int wordCount = 0;
        for (String word: words ) {
            wordCount++;
            if (wordCount % 10 == 0)
                System.out.println("Processed words: " + wordCount);
            if  (!wordGraph.containsVertex(word)) {
                wordGraph.addVertex(word);
                processWords(wordGraph, word, word, 0, categoryContext, categoryRecord);
                GraphAPI.getInstance().fixCycles(categoryRecord, wordGraph);
            }
            //System.out.println("Processed word : " + word);
        }
        //check and remove direct parent of parent node
        List<String> vertexesToRemove = new ArrayList<>();
        for (String vertex : wordGraph.vertexSet()) {
            if (wordGraph.outDegreeOf(vertex) == 0 && wordGraph.inDegreeOf(vertex) == 1) {
                Set<ConceptEdge> edges = wordGraph.incomingEdgesOf(vertex);
                ConceptEdge edge = edges.iterator().next();
                if (GraphUtils._CONCEPT_ROOT_NODE.equalsIgnoreCase(edge.getSource())) {
                    vertexesToRemove.add(vertex);
                }
            }
        }
        for (String vertex : vertexesToRemove) {
            wordGraph.removeAllEdges(GraphUtils._CONCEPT_ROOT_NODE, vertex);
            wordGraph.removeVertex(vertex);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time elapsed: " + (endTime - startTime)/ (1000) + " sec");

        //GraphUtils.exportGraph("C:\\work\\ariba\\export_test.csv", wordGraph);
        //GraphUtils.exportGraph(wordGraph, GraphUtils._INWORK_3_CSV_FILE_PATH);
        //GraphUtils.exportGraph("C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\wordGraph_100_words_in_work.csv", wordGraph);
        GraphUtils.exportGraph("C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\graphs\\chunks\\wordGraph_"+ categoryRecord.getUnspsc() + ".csv", wordGraph, CSVFormat.MATRIX);
        GraphUtils.displayGraph(wordGraph);
        return wordGraph;
    }

    int processWords(Graph<String, ConceptEdge> wordGraph, String farWord, String word, int recursionLevel, Set<String> categoryContext, UNSPSCRecord categoryRecord) {
        /*List<String> stopWords = GraphUtils.getStopWords();
        if (stopWords.contains(word)){
            System.out.println("Stop word met " + word);
            return recursionLevel;
        }*/
        if (recursionLevel < MAX_RECUSRSION_LEVEL) {
            recursionLevel++;
            boolean isLowerTreeLevel = false;
            if (recursionLevel == 1)
                isLowerTreeLevel = true;

            //Set<String> relatedWords = processingStrategy.processWordsInternally(farWord, word, isLowerTreeLevel, categoryContext, categoryRecord);
            Set<List<String>> relatedWords = processingStrategy.processWordsInternally(farWord, word, isLowerTreeLevel, categoryContext, categoryRecord);
            if (relatedWords.size() > 0) {
                //for (String relatedWord : relatedWords) {
                for(List<String> relatedWordList : relatedWords){
                    String relatedWord = relatedWordList.get(0);
                    if (!wordGraph.containsVertex(relatedWord)) {
                        wordGraph.addVertex(relatedWord);
                    }
                    GraphAPI.addEdgeToGraph(wordGraph, relatedWord, word, ConceptEdge._RELATION_TYPE_HYPERNYM, categoryRecord);
                    //possible synonyms
                    if (relatedWordList.size() > 1) {
                        for(int i = 1; i < relatedWordList.size(); i++) {
                            String synonymWord = relatedWordList.get(i);
                            if (!wordGraph.containsVertex(synonymWord)) {
                                wordGraph.addVertex(synonymWord);
                            }
                            GraphAPI.addEdgeToGraph(wordGraph, relatedWord, synonymWord, ConceptEdge._RELATION_TYPE_SYNONYM, categoryRecord);
                        }
                    }
                    //Utils.doDelay(DELAY);
                    processWords(wordGraph, farWord, Utils.getLabelFromConceptContextName(relatedWord), recursionLevel, categoryContext, categoryRecord);
                }
            }
            else {
                GraphAPI.addEdgeToGraph(wordGraph, GraphUtils._CONCEPT_ROOT_NODE, word, ConceptEdge._RELATION_TYPE_GENERIC, categoryRecord);
            }
            return recursionLevel;
        } else {
            GraphAPI.addEdgeToGraph(wordGraph, GraphUtils._CONCEPT_ROOT_NODE, word, ConceptEdge._RELATION_TYPE_GENERIC, categoryRecord);
            return recursionLevel;
        }
    }

    void enrichCategoryAttributes(UNSPSCRecord categoryRecord) {
        List<String> attributes = categoryRecord.getAttributes().get(UNSPSCRecord._ATTR_NAME_ATTRVALUE);
        List<String> products = categoryRecord.getAttributes().get(UNSPSCRecord._ATTR_NAME_PRODUCT);
        List<String> allAttributes = new ArrayList<>();
        if (attributes != null) allAttributes.addAll(attributes);
        if (products != null) allAttributes.addAll(products);
        if (allAttributes.size() >1) {

            List<String> newAttributes = new ArrayList<>();

            List<int[]> possibleIndexes = new ArrayList<>();
            int[] indexes = new int[allAttributes.size()];
            for (int i = 0; i < indexes.length; i++) {
                indexes[i] = i;
            }
            getCombination(indexes, indexes.length, 2, possibleIndexes);

            for (int[] indexCombination : possibleIndexes) {
                String newAttributeOne = allAttributes.get(indexCombination[0]) + "_" + allAttributes.get(indexCombination[1]);
                newAttributes.add(newAttributeOne);
                newAttributeOne = allAttributes.get(indexCombination[1]) + "_" + allAttributes.get(indexCombination[0]);
                newAttributes.add(newAttributeOne);
            }
            if (attributes == null) {
                attributes = new ArrayList<>();
                categoryRecord.getAttributes().put(UNSPSCRecord._ATTR_NAME_ATTRVALUE, attributes);
            }
            attributes.addAll(newAttributes);
        }
    }

    void combinationUtil(int arr[], int data[], int start, int end, int index, int r, List<int[]> possibleIndexes)
    {
        if (index == r) {
            int[] nextCombination = new int[r];
            for (int j=0; j<r; j++)
                //System.out.print(data[j]+" ");
                nextCombination[j] = data[j];
            //System.out.println("");
            possibleIndexes.add(nextCombination);
            return;
        }
        for (int i=start; i<=end && end-i+1 >= r-index; i++) {
            data[index] = arr[i];
            combinationUtil(arr, data, i+1, end, index+1, r, possibleIndexes);
        }
    }

    void getCombination(int arr[], int n, int r, List<int[]> possibleIndexes)
    {
        int data[]=new int[r];
        combinationUtil(arr, data, 0, n-1, 0, r, possibleIndexes);
    }


    static final String[] WORDS_SET1 = new String[]{
            "case", "cable", "mount", "adapter", "book", "camera", "background", "microphone", "dvd", "backpack",
            "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light", "bag",
            "plate", "monitor", "gobo", "bracket", "speaker", "box", "card", "strap", "lamp", "riflescope",
            "module", "paper", "controller", "amplifier", "battery", "headphone", "pedal", "screen", "binocular", "receiver",
            "transmitter", "tripod", "converter", "ring", "headset", "sleeve", "switch", "keyboard", "charger", "sunglass"
    };

    static final String[] WORDS_SET200 = new String[]{
            "case", "cable", "mount", "adapter", "book", "camera", "background", "microphone", "dvd", "backpack",
            "filter", "lens", "other", "stand", "cover", "software", "panel", "download", "light", "bag",
            "plate", "monitor", "gobo", "bracket", "speaker", "box", "card", "strap", "lamp", "riflescope",
            "module", "paper", "controller", "amplifier", "battery", "headphone", "pedal", "screen", "binocular", "receiver",
            "transmitter", "tripod", "converter", "ring", "headset", "sleeve", "switch", "keyboard", "charger", "sunglass",
            "string", "drive", "holder", "guitar", "connector", "clamp", "library", "knife", "plug",
            "control", "board", "supply", "film", "rack", "printer", "license", "switcher", "cart", "pack", "extender",
            "housing", "lectern", "projector", "sight", "interface", "processor", "cartridge", "mixer", "pouch", "protector",
            "upgrade", "display", "clip", "arm", "support", "unsure",  "base", "station", "head", "bundle",
            "cap", "telescope", "windscreen", "audio", "enclosure", "transceiver", "loudspeaker", "antenna", "roll", "player"
            ,
            "balm", "warp", "harddrive", "ringsight", "skateboard", "adr", "guitarist", "cablecaster", "mudbox", "searchlight",
            "cabinbag", "thesy", "corel", "replacer", "phenomenon", "casing", "monitorguard", "winzip", "saturator", "multimaximizer",
            "webplus", "resynthesis", "cartoonr", "tracktion", "bloc", "singing", "clickshop", "scooter", "translucence",  "powerkit",
            "magnifying", "xylo", "desertsuit", "aircase", "pluraleye", "rocker", "flexibellow", "survey", "lightbender", "quizcreator",
            "unicase", "supersampler", "nugen", "derechoe", "vpn", "anamorphot", "channelstrip", "keyer", "fastcut", "hardigg", "michron",
            "naturallyspeak", "tunebite", "pianocentric", "graydome", "microsdhc", "groundglass", "pochette", "microspud", "scorecleaner",
            "disguise", "podpack", "vitascene", "renaissance", "noisefree", "algorithm", "alligator", "positioning", "spectrascope", "litepanelbag",
            "binopod", "plotter", "unveil", "radar", "streetomatic", "brad", "echospace", "reality", "nametag", "stencil", "binopack", "noisemaker",
            "journalist", "parachute", "inphase", "cellist", "moviestreamer", "thepetebox", "spitfire", "globalsan", "varicam", "chesty", "multibox",
            "efex", "puck", "clic", "recharge", "waistpack", "downloadable", "minimount"
    };

    static final String[] WORDS_SET_COMP_DISPLAY = new String[]{
            "top","lcd","graphic","medical","ntsc","full","studio","hardware","computer","wireless","stand","usb",
            "displayport","tool","drawer","black","color","rack","backlit","rail","hdmi","charge","frame","desktop",
            "calibration","touchscreen","type","standard","lite","design","predator","wall","tilt","multisync","smart",
            "point","swivel","series","monitor","hub","tracking","hood","speaker","port","backlight","touch","sensor",
            "mount","view","sync","card","pattern","display","front","set","thunderbolt","angle","table","switch",
            "console","key","kit","technology","lead"
            //"touchscreen"
    };

    static final String[] WORDS_SET_COMP_INPUT_DEVICE = new String[]{
            "pillow","pack","battery","memory","dome","compact","magicwand","wireless","pitch","mat","usb","receiver","logickeyboard",
            "base","gooseneck","msi","platform","smartphone","spring","share","dataport","cable","black","foam","color","jiggler","double",
            "extend","foot","charge","management","mousepad","keycap","edge","plus","keyboard","support","leather","gel","power","magic",
            "dasher","line","station","wipe","standard","pad","medium","design","lamp","light","organizer","series","tablet","collection",
            "replacement","bluetooth","adapter","charger","air","universal","arm","mouse","supercharger","edition","case","cloth","rest",
            "bag","bar","side","control","key","manager","technology","starcraft","lead","hoverpad","cap","cleaning","overwatch"
    };

    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43212001_0613_1613.txt";
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_432117_0613_1555.txt";
    //tablets
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211509_0614_0003.txt";
    //mouse and trackballs
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211708_0613_1714.txt";
    //keyboards
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211706_0613_2339.txt";
    //scanners
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211711_0614_1346.txt";
    //Encoder decoder equipment
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211608_0614_1243.txt";
    //usb hubs and connectors
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211609_0614_1217.txt";
    //Peripheral switch boxes
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211604_0614_1114.txt";
    //43211705 Game pads or joy sticks
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_43211705_0614_0757.txt";
    //432115  Computers
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_432115_0613_1528.txt";
    //Computer printers
    //String fileName2 = "C:\\dev\\workspaces\\ao_conceptnet_github\\hyper_cnet\\data\\hwByCategory_432121_0614_0823.txt";
}
