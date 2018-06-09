package contentnet.graph;

import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.Map;

public class ConceptEdge extends DefaultEdge {

    public static final String _RELATION_TYPE_HYPERNYM = "HYPERNYM";
    public static final String _RELATION_TYPE_SYNONYM = "SYNONYM";
    public static final String _RELATION_TYPE_GENERIC = "GENERIC";

    public static final String _ATTR_KEY_UNSPSC_CATEGORY = "ATTR_KEY_UNSPSC_CATEGORY";
    public static final String _ATTR_KEY_UNSPSC_CATEGORY_NAME = "ATTR_KEY_UNSPSC_CATEGORY_NAME";

    String relationType;

    Map<String, Object> attributes = new HashMap<>();

    private String edgeSource;
    private String edgeDestination;

    public ConceptEdge() {
        super();
        relationType = _RELATION_TYPE_HYPERNYM;
    }

    public ConceptEdge(String relationType) {
        this();
        this.relationType = relationType;
    }

    public ConceptEdge(String relationType, String unspscCategory) {
        this(relationType);
        attributes.put(_ATTR_KEY_UNSPSC_CATEGORY_NAME, unspscCategory);
    }

    public ConceptEdge(String relationType, String unspscCategory, String unspscCategoryName) {
        this(relationType);
        attributes.put(_ATTR_KEY_UNSPSC_CATEGORY, unspscCategory);
        attributes.put(_ATTR_KEY_UNSPSC_CATEGORY_NAME, unspscCategoryName);
    }

    public String getSource() {
        return super.getSource().toString();
    }

    public String getTarget() {
        return super.getTarget().toString();
    }

    public String getRelationType() { return relationType; }

    public String getEdgeSource() {
        return edgeSource;
    }

    public String getEdgeDestination() {
        return edgeDestination;
    }
    public void setEdgeSourceDestination(String source, String destination) {
        edgeSource = source;
        edgeDestination = destination;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (_RELATION_TYPE_GENERIC.equalsIgnoreCase(this.relationType)) {
            sb.append(getShortRelationType());
            return sb.toString();
        }
        if (_RELATION_TYPE_SYNONYM.equalsIgnoreCase(this.relationType)) {
            /*sb.append("(")
                    .append(this.getSource()).append("=").append(this.getTarget())
                    .append("," ).append(getShortRelationType())
                    .append(")");*/
            sb.append(getShortRelationType());
            return sb.toString();
        }
        /*sb.append("(")
                .append(this.getSource()).append("->").append(this.getTarget())
                .append("," ).append(getShortRelationType())
                .append(")");*/
        sb.append(getShortRelationType());
        return sb.toString();
    }

    private String getShortRelationType() {
        if (_RELATION_TYPE_HYPERNYM.equalsIgnoreCase(this.relationType))
            return "HM";
        else if (_RELATION_TYPE_SYNONYM.equalsIgnoreCase(this.relationType))
            return "SM";
        else
            return "GEN";
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Object clone() {
        ConceptEdge newEdge = new ConceptEdge(relationType, (String)attributes.get(_ATTR_KEY_UNSPSC_CATEGORY), (String)attributes.get(_ATTR_KEY_UNSPSC_CATEGORY_NAME));
        newEdge.setEdgeSourceDestination(getEdgeSource(), getEdgeDestination());
        return newEdge;
    }
}
