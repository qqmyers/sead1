package edu.illinois.ncsa.bard.ui;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;

import edu.illinois.ncsa.bard.ISubjectSource;

public class VirtualBardGroup implements ISubjectSource, Refreshable
{
    protected Resource subject;
    protected String label;

    @Override
    public Resource getSubject()
    {
        return subject;
    }

    @Override
    public void refresh()
    {
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel( String label )
    {
        this.label = label;
    }

    public void setSubject( Resource subject )
    {
        this.subject = subject;
    }
    
    /**
     * Can either be another VirtualBardGroup or a Resource (leaf)
     * @param index
     * @return
     */
    public Object getChild( int index )
    {
        return UriRef.uriRef();
    }
}
