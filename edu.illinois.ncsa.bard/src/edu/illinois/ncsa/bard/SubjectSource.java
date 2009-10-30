package edu.illinois.ncsa.bard;

import org.tupeloproject.rdf.Resource;

public class SubjectSource implements ISubjectSource
{
    private Resource subject;

    public SubjectSource()
    {
    }

    public SubjectSource( Resource resource )
    {
        this.subject = resource;
    }

    public Resource getSubject()
    {
        return subject;
    }

    public void setSubject( Resource subject )
    {
        this.subject = subject;
    }
}
