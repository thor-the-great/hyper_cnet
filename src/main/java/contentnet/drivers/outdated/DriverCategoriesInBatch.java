package contentnet.drivers.outdated;

import contentnet.category.UNSPSCRecord;
import contentnet.data.WordProvider;
import contentnet.drivers.DriverGetContextFromCategory;
import contentnet.drivers.DriverWordsByCategory;
import contentnet.graph.ConceptEdge;
import contentnet.graph.GraphAPI;
import contentnet.graph.GraphUtils;
import org.jgrapht.Graph;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Map;

public class DriverCategoriesInBatch {

    public static void main(String[] args) {
        DriverCategoriesInBatch driver = new DriverCategoriesInBatch();
        driver.doWork();
    }

    public void doWork() {
        DriverWordsByCategory driverWordsByCategory = new DriverWordsByCategory();

        DriverGetContextFromCategory driverCategoryContexts = new DriverGetContextFromCategory();
        Map<String, UNSPSCRecord> categoryContexts = driverCategoryContexts.doWork();

        /*Map<String, String> unspscDetails = new HashMap<>();
        unspscDetails.put("unspscCategory", "432115");
        unspscDetails.put("unspscCategoryName", "Computers");

       Set<String> UNSPSC_CATEGORY_CONTEXT = new HashSet<String>() {
            {
                add("computer");
                add("computer_hardware");
                add("software");
                add("computing");
            }
        };*/
        Graph<String, ConceptEdge> wordGraph = new DirectedMultigraph<>(ConceptEdge.class);
        for (String category: categoryContexts.keySet()) {
            UNSPSCRecord categoryRecord = categoryContexts.get(category);
            //Map<String, String> unspscDetails = new HashMap<>();
            //unspscDetails.put(UNSPSCRecord._DETAIL_NAME_CATEGORY, categoryRecord.getUnspsc());
            //unspscDetails.put(UNSPSCRecord._DETAIL_NAME_CATEGORY_NAME, categoryRecord.getUnspscName());

            /*
            System.out.println("Processing category hw : " + categoryRecord.getUnspscName());

            Map<String, List<String>> attributes = categoryRecord.getAttributes();
            List<String> categoryHWSet = new ArrayList<>();
            if (attributes.get(UNSPSCRecord._ATTR_NAME_PRODUCT) != null)
                categoryHWSet.addAll(attributes.get(UNSPSCRecord._ATTR_NAME_PRODUCT));
            if (categoryHWSet.size() > 0) {
                Graph<String, ConceptEdge> wordGraphCategoryPrimaryHW = driverWordsByCategory.doWork(
                        categoryHWSet.toArray(new String[0]),
                        categoryRecord.getCategoryContext(),
                        unspscDetails,
                        null
                );
                GraphUtils.displayGraph(wordGraphCategoryPrimaryHW, categoryRecord.getUnspscName() + " based on category primary HW");
            }

            categoryHWSet.clear();
            if (attributes.get(UNSPSCRecord._ATTR_NAME_ATTRVALUE) != null)
                categoryHWSet.addAll(attributes.get(UNSPSCRecord._ATTR_NAME_ATTRVALUE));
            if (categoryHWSet.size() > 0) {
                Graph<String, ConceptEdge> wordGraphCategorySecondaryHW = driverWordsByCategory.doWork(
                        categoryHWSet.toArray(new String[0]),
                        categoryRecord.getCategoryContext(),
                        unspscDetails,
                        null
                );
                GraphUtils.displayGraph(wordGraphCategorySecondaryHW, categoryRecord.getUnspscName() + " based on category secondary HW");
            }*/

            System.out.println("Processing category : " + categoryRecord.getUnspscName());
            Graph<String, ConceptEdge> wordGraphOneCategory = driverWordsByCategory.doWork(
                    WordProvider.getAllProductSemanticWords(500).toArray(new String[0]),
                    categoryRecord.getCategoryContext(),
                    categoryRecord,
                    null
            );
            wordGraph = GraphAPI.getInstance().mergeGraphs(wordGraph, wordGraphOneCategory);
            GraphUtils.displayGraph(wordGraph);
        }
    }


}
