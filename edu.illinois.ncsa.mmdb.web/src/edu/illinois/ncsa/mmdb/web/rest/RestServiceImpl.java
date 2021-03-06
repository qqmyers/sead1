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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.input.CountingInputStream;
import org.tupeloproject.kernel.BlobRemover;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.SubjectRemover;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.impl.MemoryContext;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;

import edu.illinois.ncsa.mmdb.web.server.TupeloStore;

/**
 * SimpleRestService
 */
public class RestServiceImpl implements RestService {
    Context c = new MemoryContext();

    Context getContext() {
        return TupeloStore.getInstance().getContext();
    }

    /**
     * Create an image
     * 
     * @param imageData
     *            a stream containing image data
     * @return a new URI identifying the image
     */
    public String createImage(InputStream imageData) throws RestServiceException {
        return createImage((Map<Resource, Object>) null, imageData);
    }

    @Override
    public String mintUri(Map<Resource, Object> metadata) {
        return RestUriMinter.getInstance().mintUri(metadata);
    }

    /**
     * Create an image
     * 
     * @param metadata
     *            to give it
     * @param imageData
     *            a stream containing image data
     */
    public String createImage(Map<Resource, Object> metadata, InputStream imageData) throws RestServiceException {
        metadata.put(Rdf.TYPE, RestService.IMAGE_TYPE); // override any other type for now
        String uri = mintUri(metadata);
        if (metadata.get(LABEL_PROPERTY) == null) {
            metadata.put(LABEL_PROPERTY, uri);
        }
        createImage(uri, metadata, imageData);
        return uri;
    }

    /**
     * Create an image
     * 
     * @param imageUri
     *            the URI to give it
     * @param imageData
     *            a stream containing image data
     */
    public void createImage(String imageUri, InputStream imageData) throws RestServiceException {
        Map<Resource, Object> md = new HashMap<Resource, Object>();
        md.put(LABEL_PROPERTY, imageUri);
        updateImage(imageUri, md, imageData);
    }

    /**
     * Create an image
     * 
     * @param imageUri
     *            the URI to give it
     * @param md
     *            metadata to give it
     * @param imageData
     *            a stream containing image data
     */
    public void createImage(final String imageUri, Map<Resource, Object> md, InputStream imageData) throws RestServiceException {
        updateImage(imageUri, md, imageData);
        // do not block on this operation.
        (new Thread() {
            public void run() {
                try {
                    TupeloStore.getInstance().extractPreviews(imageUri);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * Retrieve an image. The caller <b>must</b> close the returned stream.
     * 
     * @param imageUri
     *            the URI of the image to retrieve
     * @return a stream on the image data
     */
    public InputStream retrieveImage(String imageUri) throws RestServiceException {
        try {
            return getContext().read(Resource.uriRef(imageUri));
        } catch (OperatorException e) {
            throw new RestServiceException("could not retrieve image " + imageUri, e);
        }
    }

    /**
     * Update an image
     * 
     * @param imageUri
     *            the image URI
     * @param imageData
     *            a stream on the data to replace it with
     */
    public void updateImage(String imageUri, InputStream imageData) throws RestServiceException {
        Map<Resource, Object> md = new HashMap<Resource, Object>();
        md.put(LABEL_PROPERTY, imageUri);
        updateImage(imageUri, md, imageData);
    }

    /**
     * Update an image
     * 
     * @param imageUri
     *            the image URI
     * @param metadata
     *            metadata to give it
     * @param imageData
     *            a stream on the data to replace it with
     */
    public void updateImage(String imageUri, Map<Resource, Object> metadata, InputStream imageData) throws RestServiceException {
        try {
            Resource subject = Resource.uriRef(imageUri);
            ThingSession s = new ThingSession(getContext());
            CountingInputStream cis = new CountingInputStream(imageData);
            s.write(subject, cis);
            Thing image = s.fetchThing(subject);
            image.setValue(Rdf.TYPE, IMAGE_TYPE);
            image.setValue(Files.LENGTH, cis.getByteCount());
            if (metadata != null) {
                for (Resource property : metadata.keySet() ) {
                    if (!Files.LENGTH.equals(property)) {
                        image.setValue(property, metadata.get(property));
                    }
                }
            }
            s.save();
        } catch (OperatorException e) {
            throw new RestServiceException("failed to write image " + imageUri, e);
        }
    }

    /**
     * Delete an image
     * 
     * @param imageUri
     *            the image URI
     */
    public void deleteImage(String imageUri) throws RestServiceException {
        Resource subject = Resource.uriRef(imageUri);
        try {
            ThingSession s = new ThingSession(getContext());
            s.delete(subject);
            s.save();
            BlobRemover br = new BlobRemover(Resource.uriRef(imageUri));
            getContext().perform(br);
        } catch (OperatorException e) {
            throw new RestServiceException("deletion failed for " + imageUri, e);
        }
    }

    /**
     * Create a collection
     * 
     * @param members
     *            the members of the collection (should be URI's)
     * @return a new URI identifying the collection
     */
    public String createCollection(Iterable<String> members) throws RestServiceException {
        Map<Resource, Object> md = new HashMap<Resource, Object>();
        md.put(Rdf.TYPE, RestService.COLLECTION_TYPE);
        String subject = RestUriMinter.getInstance().mintUri(md);
        updateCollection(subject, members);
        return subject;
    }

    // op: 1=replace, 2=add, 3=remove
    void mutateCollection(String collectionUri, Iterable<String> members, int op) throws RestServiceException {
        Set<Resource> memberUris = new HashSet<Resource>();
        for (String member : members ) {
            memberUris.add(Resource.uriRef(member));
        }
        try {
            ThingSession ts = new ThingSession(getContext());
            Thing collection = ts.fetchThing(Resource.uriRef(collectionUri));
            collection.addType(COLLECTION_TYPE);
            switch (op) {
                case 1: // replace
                    collection.setValues(HAS_MEMBER, memberUris);
                    break;
                case 2: // add
                    collection.addValues(HAS_MEMBER, memberUris);
                    break;
                case 3: // remove
                    collection.removeValues(HAS_MEMBER, memberUris);
                    break;
            }
            ts.save();
        } catch (OperatorException e) {
            throw new RestServiceException("collection update failed for " + collectionUri);
        }
    }

    /**
     * Update a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @param members
     *            the new set of members (should be URI's)
     */
    public void updateCollection(String collectionUri, Iterable<String> members) throws RestServiceException {
        mutateCollection(collectionUri, members, 1);
    }

    /**
     * Retrieve a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @return the members (a list of URI's)
     */
    public List<String> retrieveCollection(String collectionUri) throws RestServiceException {
        List<String> result = new LinkedList<String>();
        try {
            for (Triple t : getContext().match(Resource.uriRef(collectionUri), HAS_MEMBER, null) ) {
                result.add(t.getObject().getString());
            }
        } catch (OperatorException e) {
            throw new RestServiceException("could not fetch collection " + collectionUri);
        }
        return result;
    }

    /**
     * Delete a collection
     * 
     * @param collectionUri
     *            the collection URI
     */
    public void deleteCollection(String collectionUri) throws RestServiceException {
        try {
            SubjectRemover sr = new SubjectRemover();
            sr.setSubject(Resource.uriRef(collectionUri));
            getContext().perform(sr);
        } catch (OperatorException e) {
            throw new RestServiceException("could not remove collection " + collectionUri);
        }
    }

    /**
     * Add to a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @param members
     *            the members to add (should be URI's)
     */
    public void addToCollection(String collectionUri, Iterable<String> members) throws RestServiceException {
        mutateCollection(collectionUri, members, 2);
    }

    /**
     * Remove from a collection
     * 
     * @param collectionUri
     *            the collection URI
     * @param members
     *            the members to remove (should be URI's)
     */
    public void removeFromCollection(String collectionUri, Iterable<String> members) throws RestServiceException {
        mutateCollection(collectionUri, members, 3);
    }

    /**
     * Add tag(s) to a resource
     * 
     * @param resourceUri
     *            the resource URI
     * @param tags
     *            tags to add (should be tag-like strings (e.g., no spaces))
     */
    public void addTags(String resourceUri, Iterable<String> tags) {
    }

    /**
     * Remove tag(s) from a resource
     * 
     * @param resourceUri
     *            the resource URI
     * @param tags
     *            the tags to remove (should be tag-like strings (e.g., no
     *            spaces))
     */
    public void removeTags(String resourceUri, Iterable<String> tags) {
    }

    /**
     * Rate the resource
     * 
     * @param resourceUri
     *            the resource URI
     * @param rating
     *            1-5
     */
    public void setRating(String resourceUri, int rating) {
    }

    /**
     * Retrieve the user's rating for the resource
     * 
     * @param resourceUri
     *            the resource URI
     * @return the rating (1-5)
     */
    public int getRating(String resourceUri) {
        return 0;
    }
}
