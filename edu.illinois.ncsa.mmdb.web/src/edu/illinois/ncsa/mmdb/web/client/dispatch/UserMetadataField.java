package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class UserMetadataField implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 4553235384429344720L;

    public static final int   PLAIN            = 0;
    public static final int   DATATYPE         = 1;
    public static final int   ENUMERATED       = 2;
    public static final int   CLASS            = 3;

    //START - ADDED BY RAM
    //A metadata field to set/ identify "Creator" of a dataset
    public static final int   VIVO_CREATOR     = 4;
    public static final int   VIVO_PART_OF     = 5;
    public static final int   VIVO_CONTACT     = 6;
    //END - ADDED BY RAM

    /** For maxCardinality, indicates no upper bound */
    public static final int   UNBOUNDED        = -1;

    String                    uri;                                    // ID
    String                    label;                                  // user-readable name
    int                       type             = PLAIN;               // property type
    String                    datatype;                               // datatype URI, for DATATYPE properties
    Set<NamedThing>           range;                                  // range; interpretation depends on type
    int                       minCardinality;
    int                       maxCardinality;

    public UserMetadataField() {
    }

    String creatorLabel = "creator";
    String contactLabel = "contact name";
    String partOfLabel  = "published in";

    public UserMetadataField(String uri, String label) {
        setUri(uri);
        setLabel(label);

        //START - ADDED BY RAM
        //Make provision to get the type metadata field type as VIVO if the label is creator
        if (label.toLowerCase().contains(creatorLabel)) {
            setType(VIVO_CREATOR);
        } else if (label.toLowerCase().contains(contactLabel)) {
            setType(VIVO_CONTACT);
        } else if (label.toLowerCase().contains(partOfLabel)) {
            setType(VIVO_PART_OF);
        } else {
            setType(PLAIN);
        }
        //END - ADDED BY RAM
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public Set<NamedThing> getRange() {
        if (range == null) {
            range = new HashSet<NamedThing>();
        }
        return range;
    }

    public void setRange(Set<NamedThing> range) {
        this.range = range;
    }

    public void addToRange(String uri, String name) {
        NamedThing n = new NamedThing();
        n.setUri(uri);
        n.setName(name);
        getRange().add(n);
    }

    public int getMinCardinality() {
        return minCardinality;
    }

    public void setMinCardinality(int minCardinality) {
        this.minCardinality = minCardinality;
    }

    public int getMaxCardinality() {
        return maxCardinality;
    }

    public void setMaxCardinality(int maxCardinality) {
        this.maxCardinality = maxCardinality;
    }

    public void setRequired() {
        minCardinality = 1;
    }

    public boolean isRequired() {
        return minCardinality > 0;
    }

    public boolean isMultivalued() {
        return maxCardinality == UNBOUNDED || maxCardinality > 1;
    }

    public void setMultivalued() {
        maxCardinality = UNBOUNDED;
    }
}
