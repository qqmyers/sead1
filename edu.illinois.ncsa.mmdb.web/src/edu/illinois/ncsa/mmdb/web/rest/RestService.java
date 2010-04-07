/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010, NCSA.  All rights reserved.
 *
 * Developed by:
 * Cyberenvironments and Technologies (CET)
 * http://cet.ncsa.illinois.edu/
 *
 * National Center for Supercomputing Applications (NCSA)
 * http://www.ncsa.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the 
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * - Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimers.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimers in the
 *   documentation and/or other materials provided with the distribution.
 * - Neither the names of CET, University of Illinois/NCSA, nor the names
 *   of its contributors may be used to endorse or promote products
 *   derived from this Software without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *******************************************************************************/
package edu.illinois.ncsa.mmdb.web.rest;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Files;

import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.workflow.Cyberintegrator;

/**
 * RestService
 */
public interface RestService {
    public static final Resource IMAGE_TYPE        = Cyberintegrator.DATASET;
    public static final Resource COLLECTION_TYPE   = CollectionBeanUtil.COLLECTION_TYPE;
    public static final Resource HAS_MEMBER        = CollectionBeanUtil.DCTERMS_HAS_PART;
    public static final Resource LABEL_PROPERTY    = Dc.TITLE;
    public static final Resource DATE_PROPERTY     = Dc.DATE;
    public static final Resource FORMAT_PROPERTY   = Dc.FORMAT;
    public static final Resource FILENAME_PROPERTY = Files.HAS_NAME;                     // FIXME undeprecate

    // image CRUD
    /**
     * Create an image
     * 
     * @param imageData
     *            a stream containing image data
     * @return a new URI identifying the image
     */
    String createImage(InputStream imageData) throws RestServiceException;

    /**
     * Create an image
     * 
     * @param metadata
     *            to give it
     * @param imageData
     *            a stream containing image data
     */
    String createImage(Map<Resource, Object> metadata, InputStream imageData) throws RestServiceException;

    /**
     * Create an image
     * 
     * @param imageUri
     *            the URI to give it
     * @param imageData
     *            a stream containing image data
     */
    void createImage(String imageUri, InputStream imageData) throws RestServiceException;

    /**
     * Create an image
     * 
     * @param imageUri
     *            the URI to give it
     * @param metadata
     *            to give it
     * @param imageData
     *            a stream containing image data
     */
    void createImage(String imageUri, Map<Resource, Object> metadata, InputStream imageData) throws RestServiceException;

    /**
     * Retrieve an image
     * 
     * @param imageUri
     *            the URI of the image to retrieve
     * @return a stream on the image data
     */
    InputStream retrieveImage(String imageUri) throws RestServiceException;

    /**
     * Update an image
     * 
     * @param imageUri
     *            the image URI
     * @param imageData
     *            a stream on the data to replace it with
     */
    void updateImage(String imageUri, InputStream imageData) throws RestServiceException;

    /**
     * Update an image
     * 
     * @param imageUri
     *            the image URI
     * @param metadata
     *            to give it
     * @param imageData
     *            a stream on the data to replace it with
     */
    void updateImage(String imageUri, Map<Resource, Object> metadata, InputStream imageData) throws RestServiceException;

    /**
     * Delete an image
     * 
     * @param imageUri
     *            the image URI
     */
    void deleteImage(String imageUri) throws RestServiceException;

    // collection CRUD

    /**
     * Create a collection
     * 
     * @param members
     *            the members of the collection (should be URI's)
     * @return a new URI identifying the collection
     */
    String createCollection(Iterable<String> members) throws RestServiceException;

    /**
     * Update a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @param members
     *            the new set of members (should be URI's)
     */
    void updateCollection(String collectionUri, Iterable<String> members) throws RestServiceException;

    /**
     * Retrieve a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @return the members (a list of URI's)
     */
    List<String> retrieveCollection(String collectionUri) throws RestServiceException;

    /**
     * Delete a collection
     * 
     * @param collectionUri
     *            the collection URI
     */
    void deleteCollection(String collectionUri) throws RestServiceException;

    // collection add/remove

    /**
     * Add to a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @param members
     *            the members to add (should be URI's)
     */
    void addToCollection(String collectionUri, Iterable<String> members) throws RestServiceException;

    /**
     * Remove from a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @param members
     *            the members to remove (should be URI's)
     */
    void removeFromCollection(String collectionUri, Iterable<String> members) throws RestServiceException;

    // tagging

    /**
     * Add tag(s) to a resource
     * 
     * @param resourceUri
     *            the resource URI
     * @param tags
     *            tags to add (should be tag-like strings (e.g., no spaces))
     */
    void addTags(String resourceUri, Iterable<String> tags);

    /**
     * Remove tag(s) from a resource
     * 
     * @param resourceUri
     *            the resource URI
     * @param tags
     *            the tags to remove (should be tag-like strings (e.g., no
     *            spaces))
     */
    void removeTags(String resourceUri, Iterable<String> tags);

    // rating

    /**
     * Rate the resource
     * 
     * @param resourceUri
     *            the resource URI
     * @param rating
     *            1-5
     */
    void setRating(String resourceUri, int rating);

    /**
     * Retrieve the user's rating for the resource
     * 
     * @param resourceUri
     *            the resource URI
     * @return the rating (1-5)
     */
    int getRating(String resourceUri);

    /**
     * Generate a URI for an entity (e.g., image) with the given metadata
     * 
     * @param metadata
     * @return
     */
    String mintUri(Map<Resource, Object> metadata);
}
