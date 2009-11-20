package edu.illinois.ncsa.mmdb.web.rest;

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
}
