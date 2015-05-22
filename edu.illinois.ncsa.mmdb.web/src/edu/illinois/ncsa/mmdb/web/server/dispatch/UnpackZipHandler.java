/*******************************************************************************
 * Copyright 2014 University of Michigan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipInputStream;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BlobFetcher;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Thing;
import org.tupeloproject.kernel.ThingSession;
import org.tupeloproject.kernel.TripleWriter;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.UnpackZip;
import edu.illinois.ncsa.mmdb.web.client.dispatch.UnpackZipResult;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.rest.RestService;
import edu.illinois.ncsa.mmdb.web.rest.RestUriMinter;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.resteasy.PermissionCheck;
import edu.illinois.ncsa.mmdb.web.server.util.BeanFiller;
import edu.illinois.ncsa.mmdb.web.server.util.CollectionUnzipper;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;

/**
 * Unpack a zip file to create collections and datasets. Will follow
 * subcollection hierarchy, will not open zip files included in the outer zip.
 *
 * @author myersjd@umich.edu
 *
 */
public class UnpackZipHandler implements ActionHandler<UnpackZip, UnpackZipResult> {

    /** Commons logging **/
    private static Log  log                  = LogFactory.getLog(UnpackZipHandler.class);

    static final String seadWasExtractedFrom = "http://sead-data.net/terms/wasExtractedFrom";

    @Override
    public UnpackZipResult execute(UnpackZip action, ExecutionContext context) throws ActionException {

        UriRef creator = Resource.uriRef(action.getUser());
        log.debug("In UnpackZipHandler");

        //Check if unpacking has already been done
        Unifier u = new Unifier();
        u.addPattern("collection", Resource.uriRef(seadWasExtractedFrom), Resource.uriRef(action.getUri()));
        u.addPattern("collection", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
        u.setColumnNames("collection");
        String collectionUri = null;
        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "collection") ) {
                collectionUri = row.get(0).getString();
            }
        } catch (OperatorException e1) {
            log.error(e1);
        }
        if (collectionUri != null) {
            //If so, return the existing uri
            UnpackZipResult uzr = new UnpackZipResult();
            uzr.setUri(collectionUri);
            return uzr;
        }

        PermissionCheck p = new PermissionCheck(creator, Permission.ADD_COLLECTION);
        if (!p.userHasPermission()) {
            throw new ActionException("Permission Failure: " + p.getErrorResponse().getStatus());
        }

        InputStream is = null;
        UnpackZipResult uzr = new UnpackZipResult();

        String datasetUri = action.getUri();
        String name = action.getName();
        //Strip zip
        if (name.endsWith(".zip")) {
            name = name.substring(0, name.length() - 4);
        }
        // Create collection uri
        Map<Resource, Object> md = new LinkedHashMap<Resource, Object>();
        md.put(Rdf.TYPE, RestService.COLLECTION_TYPE);
        UriRef uri = Resource.uriRef(RestUriMinter.getInstance().mintUri(md));
        log.debug("Reserving Collection: " + name + ": " + uri.toString() + "\n");

        try {

            Context c = TupeloStore.getInstance().getContext();
            BlobFetcher bf = new BlobFetcher();
            bf.setSubject(Resource.uriRef(action.getUri()));
            log.debug("BF subject is " + action.getUri());
            c.perform(bf);
            is = bf.getInputStream();
            ZipInputStream zis = new ZipInputStream(is);
            log.debug("Opened Streams");

            uzr.setUri(CollectionUnzipper.unpackContents(zis, uri.toString(), creator));
            zis.close();
            if (uzr.getUri() != null) {
                //Nothing went wrong, so create parent collection and return its uri
                ThingSession ts = c.getThingSession();
                Thing t = ts.newThing(uri);
                BeanFiller.fillCollectionBean(t, name, creator, new Date());
                ts.save();
                ts.close();
                //Relate zip file and collection
                //FixMe - make this a relationship when relationships can handle collections?
                TripleWriter tw = new TripleWriter();
                tw.add(uri, Resource.uriRef(seadWasExtractedFrom), Resource.uriRef(datasetUri));
                //Mark as top level
                tw.add(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, uri);
                c.perform(tw);
            } else {
                throw new ActionException("Failed to unpack zip file");
            }

        } catch (IOException e) {
            log.warn(e);
            throw new ActionException(e);
        } catch (OperatorException e) {
            log.warn(e);
            throw new ActionException(e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        }

        return uzr;
    }

    @Override
    public void rollback(UnpackZip action, UnpackZipResult result, ExecutionContext context) throws ActionException {
        // TODO Auto-generated method stub

    }

    @Override
    public Class<UnpackZip> getActionType() {
        return UnpackZip.class;
    }
}
