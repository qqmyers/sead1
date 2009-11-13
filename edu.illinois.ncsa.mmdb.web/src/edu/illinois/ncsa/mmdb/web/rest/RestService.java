package edu.illinois.ncsa.mmdb.web.rest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;

import edu.uiuc.ncsa.cet.bean.tupelo.workflow.Cyberintegrator;

/**
 * RestService
 */
public interface RestService {
    public static final Resource IMAGE_TYPE = Cyberintegrator.DATASET;
    public static final Resource COLLECTION_TYPE = Cet.cet("mmdb/Collection");
    public static final Resource HAS_MEMBER = Cet.cet("mmdb/hasMember"); // maybe dcterms:hasPart?
    public static final Resource LABEL_PROPERTY = Dc.TITLE;
    public static final Resource DATE_PROPERTY = Dc.DATE;
    public static final Resource FORMAT_PROPERTY = Dc.FORMAT;

    // image CRUD
    /**
     * Create an image
     * @param imageData a stream containing image data
     * @return a new URI identifying the image
     */
    String createImage(InputStream imageData) throws RestServiceException;

    /**
     * Create an image
     * @param metadata to give it
     * @param imageData a stream containing image data
     */
    String createImage(Map<Resource,Object> metadata, InputStream imageData) throws RestServiceException;

    /**
     * Create an image
     * @param imageUri the URI to give it
     * @param imageData a stream containing image data
     */
    void createImage(String imageUri, InputStream imageData) throws RestServiceException;

    /**
     * Create an image
     * @param imageUri the URI to give it
     * @param metadata to give it
     * @param imageData a stream containing image data
     */
    void createImage(String imageUri, Map<Resource,Object> metadata, InputStream imageData) throws RestServiceException;

    /**
     * Retrieve an image
     * @param imageUri the URI of the image to retrieve
     * @return a stream on the image data
     */
    InputStream retrieveImage(String imageUri) throws RestServiceException;

    /**
     * Update an image
     * @param imageUri the image URI
     * @param imageData a stream on the data to replace it with
     */
    void updateImage(String imageUri, InputStream imageData) throws RestServiceException;

    /**
     * Update an image
     * @param imageUri the image URI
     * @param metadata to give it
     * @param imageData a stream on the data to replace it with
     */
    void updateImage(String imageUri, Map<Resource,Object> metadata, InputStream imageData) throws RestServiceException;

    /**
     * Delete an image
     * @param imageUri the image URI
     */
    void deleteImage(String imageUri) throws RestServiceException;

    // collection CRUD

    /**
     * Create a collection
     * @param members the members of the collection (should be URI's)
     * @return a new URI identifying the collection
     */
    String createCollection(Iterable<String> members) throws RestServiceException;

    /**
     * Update a collection
     * @param collectionUri the collection URI
     * @param members the new set of members (should be URI's)
     */
    void updateCollection(String collectionUri, Iterable<String> members) throws RestServiceException;

    /**
     * Retrieve a collection
     * @param collectionUri the collection URI
     * @return the members (a list of URI's)
     */
    List<String> retrieveCollection(String collectionUri) throws RestServiceException;

    /**
     * Delete a collection
     * @param collectionUri the collection URI
     */
    void deleteCollection(String collectionUri) throws RestServiceException;

    // collection add/remove

    /**
     * Add to a collection
     * @param collectionUri the collection URI
     * @param members the members to add (should be URI's)
     */
    void addToCollection(String collectionUri, Iterable<String> members) throws RestServiceException;

    /**
     * Remove from a collection
     * @param collectionUri the collection URI
     * @param members the members to remove (should be URI's)
     */
    void removeFromCollection(String collectionUri, Iterable<String> members) throws RestServiceException;

    // tagging

    /**
     * Add tag(s) to a resource
     * @param resourceUri the resource URI
     * @param tags tags to add (should be tag-like strings (e.g., no spaces))
     */
    void addTags(String resourceUri, Iterable<String> tags);

    /**
     * Remove tag(s) from a resource
     * @param resourceUri the resource URI
     * @param tags the tags to remove (should be tag-like strings (e.g., no spaces))
     */
    void removeTags(String resourceUri, Iterable<String> tags);

    // rating

    /**
     * Rate the resource
     * @param resourceUri the resource URI
     * @param rating 1-5
     */
    void setRating(String resourceUri, int rating);

    /**
     * Retrieve the user's rating for the resource
     * @param resourceUri the resource URI
     * @return the rating (1-5)
     */
    int getRating(String resourceUri);
}
