package edu.illinois.ncsa.mmdb.web.client.dispatch;

/**
 * Call the extraction service from the web client
 * 
 * @author Rob Kooper
 * 
 */
@SuppressWarnings("serial")
public class ExtractionService extends SubjectAction<ExtractionServiceResult>
{

    @SuppressWarnings("unused")
    private ExtractionService()
    {
    }

    public ExtractionService( String uri )
    {
        super( uri );
    }
}
