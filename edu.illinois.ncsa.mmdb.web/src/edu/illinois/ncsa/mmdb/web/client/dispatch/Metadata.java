package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Metadata implements Serializable, Comparable<Metadata> {
    private String category;
    private String label;
    private String value;
    
    public Metadata() {
    }
    
    public Metadata(String category, String label, String value) {
        this.category = category;
        this.label = label;
        this.value = value;
    }

    /**
     * @return the category
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory( String category )
    {
        this.category = category;
    }

    /**
     * @return the label
     */
    public String getLabel()
    {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel( String label )
    {
        this.label = label;
    }

    /**
     * @return the value
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue( String value )
    {
        this.value = value;
    }
    
    @Override
    public int compareTo( Metadata o )
    {
        if (category.equals( o.category )) {
            if (label.equals( o.label )) {
                return value.compareTo( o.value );
            } else {
                return label.compareTo( o.label );
            }
        } else {
            return category.compareTo( o.category );
        }
    }

}