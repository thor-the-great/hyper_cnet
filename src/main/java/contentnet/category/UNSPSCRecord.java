package contentnet.category;

import java.util.*;

public class UNSPSCRecord {
    String unspsc;
    String unspscName;
    Map<String, List<String>> attributes = new HashMap<>();

    public static final String _ATTR_NAME_PRODUCT = "product";
    public static final String _ATTR_NAME_ATTRVALUE = "attr_value";

    public static final String _DETAIL_NAME_CATEGORY = "unspscCategory";
    public static final String _DETAIL_NAME_CATEGORY_NAME = "unspscCategoryName";

    Set<String> categoryContext = new HashSet<>();

    public String getUnspsc() {
        return unspsc;
    }

    public void setUnspsc(String unspsc) {
        this.unspsc = unspsc;
    }

    public String getUnspscName() {
        return unspscName;
    }

    public void setUnspscName(String unspscName) {
        this.unspscName = unspscName;
    }

    public Map<String, List<String>> getAttributes() {
        return attributes;
    }

    public Set<String> getCategoryContext() {
        return categoryContext;
    }
}