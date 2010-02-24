package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

/**
 * Return the job id of the extraction service.
 * 
 * @author Rob Kooper
 * 
 */
public class ExtractionServiceResult implements Result
{
    private static final long serialVersionUID = 1L;

    private String            jobid            = null;

    public ExtractionServiceResult()
    {
    }

    public ExtractionServiceResult( String jobid )
    {
        setJobid( jobid );
    }

    public void setJobid( String jobid )
    {
        this.jobid = jobid;
    }

    public String getJobid()
    {
        return jobid;
    }
}
