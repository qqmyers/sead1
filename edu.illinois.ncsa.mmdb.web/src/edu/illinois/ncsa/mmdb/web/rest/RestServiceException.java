package edu.illinois.ncsa.mmdb.web.rest;

import org.tupeloproject.kernel.NotFoundException;

/**
 * RestServiceException
 */
public class RestServiceException extends Exception {
    public RestServiceException(String msg) {
        super(msg);
    }
    public RestServiceException(String msg, Throwable cause) {
        super(msg,cause);
    }
    
    public boolean isNotFound() {
    	return getCause() instanceof NotFoundException;
    }
}
