package edu.illinois.ncsa.bard;

public class SimpleMimeTypeContainer implements HasMimeType
{
    private String mimeType;
    
    public SimpleMimeTypeContainer()
    {
    }

    public SimpleMimeTypeContainer( String mimeType )
    {
        super();
        this.mimeType = mimeType;
    }

    public void setMimeType( String mimeType )
    {
        this.mimeType = mimeType;
    }

    @Override
    public String getMimeType()
    {
        return mimeType;
    }
}
