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
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.TripleMatcher;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.ObjectResourceMapping;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.Triple;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import com.hp.hpl.jena.vocabulary.DCTerms;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult.ListQueryItem;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoCollectionBean;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoPointBean;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoserverCollectionBean;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.CollectionBean;
import edu.uiuc.ncsa.cet.bean.DatasetBean;
import edu.uiuc.ncsa.cet.bean.PreviewBean;
import edu.uiuc.ncsa.cet.bean.PreviewGeoserverBean;
import edu.uiuc.ncsa.cet.bean.PreviewImageBean;
import edu.uiuc.ncsa.cet.bean.PreviewMultiImageBean;
import edu.uiuc.ncsa.cet.bean.gis.GeoPointBean;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.DatasetBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewGeoserverBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PreviewImageBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.gis.GeoPointBeanUtil;

/**
 * Get datasets in a paricular collection.
 *
 * @author lmarini
 * @author myersjd@umich.edu
 *
 */
public class GetCollectionHandler implements
        ActionHandler<GetCollection, GetCollectionResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetCollectionHandler.class);

    @Override
    public GetCollectionResult execute(GetCollection arg0, ExecutionContext arg1)
            throws ActionException {
        log.trace("Getting " + arg0.getUri());
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);

        try {
            CollectionBean collectionBean = fastGetCollection(cbu, arg0.getUri());

            // FIXME this might be slow for large collections, although its result is memoized per collection
            //int collectionSize = TupeloStore.getInstance().countDatasets(arg0.getUri(), false);

            GetCollectionResult result = new GetCollectionResult(collectionBean, 0);

            getCollectionPreviews(arg0.getUser(), arg0.getUri(), result);

            String doiString = getDOI(arg0.getUri());
            if (doiString != null) {
                result.setDOI(doiString);
            }

            getParent(arg0.getUri(), result);

            return result;
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }

    private void getParent(String uri, GetCollectionResult result) throws OperatorException {
        Unifier u = new Unifier();
        UriRef collUri = Resource.uriRef(uri);
        u.addPattern("parent", DcTerms.HAS_PART, collUri);
        u.addPattern("parent", Dc.TITLE, "title", true);
        u.setColumnNames("parent", "title");
        HashMap<String, String> parents = new HashMap<String, String>();
        TupeloStore.getInstance().unifyExcludeDeleted(u, "parent");
        for (Tuple<Resource> row : u.getResult() ) {
            parents.put(row.get(0).toString(), row.get(1).toString());
        }
        result.setParents(parents);
    }

    // FIXME use Rob's BeanFactory instead of this hardcoded way
    private CollectionBean fastGetCollection(CollectionBeanUtil cbu, String uriString) throws OperatorException {
        PersonBeanUtil pbu = new PersonBeanUtil(cbu.getBeanSession());
        Unifier u = new Unifier();
        UriRef uri = Resource.uriRef(uriString);
        u.addPattern(uri, Rdf.TYPE, cbu.getType());
        u.addPattern(uri, Dc.CREATOR, "creator", true);
        u.addPattern(uri, Dc.TITLE, "title", true);
        u.addPattern(uri, Dc.DESCRIPTION, "description", true);
        u.addPattern(uri, DcTerms.DATE_CREATED, "dateCreated", true);
        u.addPattern(uri, DcTerms.DATE_MODIFIED, "dateModified", true);
        u.setColumnNames("creator", "title", "description", "dateCreated", "dateModified");
        CollectionBean colBean = new CollectionBean();
        log.trace("Retrieving info");
        TupeloStore.getInstance().getContext().perform(u);

        for (Tuple<Resource> row : u.getResult() ) {
            log.debug(row);
            int r = 0;
            UriRef creator = (UriRef) row.get(r++);
            Resource title = row.get(r++);
            Resource description = row.get(r++);
            Resource dateCreated = row.get(r++);
            Resource dateModified = row.get(r++);
            colBean.setUri(uriString);
            if (creator != null) {
                colBean.setCreator(pbu.get(creator));
            }
            if (title != null) {
                colBean.setTitle(title.getString());
            }
            if (description != null) {
                colBean.setDescription(description.getString());
            }
            if (dateCreated != null) {
                colBean.setCreationDate((Date) ObjectResourceMapping.object(dateCreated));
            }
            if (dateModified != null) {
                colBean.setLastModifiedDate((Date) ObjectResourceMapping.object(dateModified));
            }
            return colBean;
        }
        return null;
    }

    private void getCollectionPreviews(String user, String collectionUri, GetCollectionResult result) {
        ArrayList<PreviewBean> previews = new ArrayList<PreviewBean>();
        result.setPreviews(previews);
        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        try {
            previews.addAll(new PreviewImageBeanUtil(beanSession).getAssociationsFor(collectionUri));
        } catch (OperatorException e) {
            log.error("Error retrieving collection with uri " + collectionUri, e);
            e.printStackTrace();
        }

        PreviewImageBeanUtil pibu = new PreviewImageBeanUtil(beanSession);
        PreviewGeoserverBeanUtil pgbu = new PreviewGeoserverBeanUtil(beanSession);
        DatasetBeanUtil dbu = new DatasetBeanUtil(beanSession);
        GeoPointBeanUtil gpbu = new GeoPointBeanUtil(beanSession);

        // Get a list of URI's for the datasets associated with the collection from GetPreviews
        List<String> datasetUris = getDatasets(user, collectionUri);
        result.setCollectionSize(datasetUris.size());

        ArrayList<PreviewImageBean> previewImages = new ArrayList<PreviewImageBean>();

        PreviewGeoPointBean previewGeoPointBean = new PreviewGeoPointBean();
        PreviewGeoserverCollectionBean previewGeoserverCollecitonBean = new PreviewGeoserverCollectionBean();

        PreviewGeoCollectionBean previewGeoCollecitonBean = new PreviewGeoCollectionBean();

        for (String datasetUri : datasetUris ) {
            log.info("processing datasetUri = " + datasetUri);
            try {
                DatasetBean dataset = dbu.get(datasetUri);
                // Grab the small preview image uri associated with the given dataset
                String smallImageUri = TupeloStore.getInstance().getPreviewUri(datasetUri, GetPreviews.SMALL);

                if (smallImageUri != null) {
                    try {
                        // Grab the image bean.
                        PreviewImageBean preview = pibu.get(smallImageUri, true);
                        preview.setLabel(datasetUri);
                        previewImages.add(preview);
                    } catch (OperatorException e) {
                        log.error("Could not get small preview for dataset with uri = " + datasetUri, e);
                    }
                }

                try {
                    Collection<GeoPointBean> associations = gpbu.getAssociationsFor(datasetUri);
                    Iterator<GeoPointBean> iter = associations.iterator();
                    while (iter.hasNext()) {
                        GeoPointBean next = iter.next();
                        previewGeoPointBean.add(next, dataset);
                    }
                } catch (OperatorException e) {
                    log.warn("Could not get geo points for dataset with uri = " + datasetUri, e);
                }

                try {
                    Collection<PreviewGeoserverBean> associations = pgbu.getAssociationsFor(datasetUri);
                    Iterator<PreviewGeoserverBean> iter = associations.iterator();
                    while (iter.hasNext()) {
                        PreviewGeoserverBean next = iter.next();
                        previewGeoserverCollecitonBean.add(next, dataset);
                    }
                } catch (OperatorException e) {
                    log.warn("Could not get geoserver preview for dataset with uri = " + datasetUri, e);
                }

            } catch (Exception e1) {
                log.error("Could not get DatasetBean with uri = " + datasetUri, e1);
            }
        }
        if (!previewImages.isEmpty()) {
            PreviewMultiImageBean multiImagePreview = new PreviewMultiImageBean();
            multiImagePreview.setImages(previewImages);

            previews.add(multiImagePreview);
        }

        boolean addPreviewGeo = false;
        if (!previewGeoPointBean.getGeoPoints().isEmpty()) {
            previewGeoCollecitonBean.setPreviewGeoPointBean(previewGeoPointBean);
            addPreviewGeo = true;
        }

        if (!previewGeoserverCollecitonBean.getPreviewGeoservers().isEmpty()) {
            previewGeoCollecitonBean.setPreviewGeoserverCollectionBean(previewGeoserverCollecitonBean);
            addPreviewGeo = true;
        }

        if (addPreviewGeo) {
            previews.add(previewGeoCollecitonBean);
        }
    }

    public static List<String> getDatasets(String user, String collection) {
        Unifier u = new Unifier();

        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        int userlevel = rbac.getUserAccessLevel(Resource.uriRef(user));
        int defaultlevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));

        u.addPattern(Resource.uriRef(collection), DcTerms.HAS_PART, "s");
        u.addPattern("s", Rdf.TYPE, Cet.DATASET);
        u.addColumnName("s");

        // add all items we might need
        u.addPattern("s", Rdf.TYPE, "t");
        u.addColumnName("t");
        u.addPattern("s", Dc.DATE, "d1", true);
        u.addColumnName("d1");
        u.addPattern("s", DcTerms.DATE_CREATED, "d2", true);
        u.addColumnName("d2");
        u.addColumnName("n");
        u.addPattern("s", Dc.CREATOR, "a");
        u.addColumnName("a");
        u.addColumnName("l");
        u.addColumnName("f");
        String pred = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
        u.addPattern("s", Resource.uriRef(pred), "r", true);
        u.addColumnName("r");

        // limit results
        // TODO this does not work for categories
        //        if (listquery.getLimit() > 0) {
        //            u.setLimit(listquery.getLimit());
        //        }
        //        u.setOffset(listquery.getOffset());

        // s t d1 d2 n a l f r

        // fetch results
        final Map<String, ListQueryItem> map = new HashMap<String, ListQueryItem>();
        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());
        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "s") ) {
                if ("tag:tupeloproject.org,2006:/2.0/beans/2.0/storageTypeBeanEntry".equals(row.get(1).getString())) {
                    continue;
                }
                if (map.containsKey(row.get(0).getString())) {
                    log.warn("Already contain item for " + row);
                    continue;
                }
                if (Cet.DATASET.equals(row.get(1)) && !row.get(5).getString().equals(user)) {
                    int datasetlevel = (row.get(8) != null) ? Integer.parseInt(row.get(8).getString()) : defaultlevel;
                    if (datasetlevel < userlevel) {
                        continue;
                    }
                }
                ListQueryItem item = new ListQueryItem();
                map.put(row.get(0).getString(), item);

                item.setUri(row.get(0).getString());
                item.setAuthor(pbu.get(row.get(5)).getName());
                if (row.get(2) != null) {
                    if (row.get(2).asObject() instanceof Date) {
                        item.setDate((Date) row.get(2).asObject());
                    }
                } else {
                    if (row.get(3).asObject() instanceof Date) {
                        item.setDate((Date) row.get(3).asObject());
                    }
                }
            }

            List<String> uris = new ArrayList<String>(map.keySet());
            Collections.sort(uris, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    ListQueryItem item1 = map.get(o1);
                    if (item1 == null) {
                        return -1;
                    }
                    ListQueryItem item2 = map.get(o2);
                    if (item2 == null) {
                        return +1;
                    }
                    if (item1.getDate() == null) {
                        return -1;
                    }
                    if (item2.getDate() == null) {
                        return +1;
                    }
                    return item1.getDate().compareTo(item2.getDate());
                }
            });
            return uris;
        } catch (OperatorException exc) {
            log.error("Could not fetch items.", exc);
            return new ArrayList<String>();
        }
    }

    private String getDOI(String collectionUri) {
        try {
            TripleMatcher tm = new TripleMatcher();
            tm.setSubject(Resource.uriRef(collectionUri));
            tm.setPredicate(Resource.uriRef(DCTerms.identifier.getURI()));
            TupeloStore.getInstance().getContext().perform(tm);
            Set<Triple> results = tm.getResult();
            //Assuming single value for now/using first returned value
            if (results.size() > 0) {
                Iterator<Triple> it = results.iterator();
                return (it.next().getObject().toString());
            } else {
                log.debug("No DOI for " + collectionUri);
                return null;
            }
        } catch (Throwable thr) {
            log.debug("Error getting DOI", thr);
        }
        return null;
    }

    @Override
    public Class<GetCollection> getActionType() {
        return GetCollection.class;
    }

    @Override
    public void rollback(GetCollection arg0, GetCollectionResult arg1,
            ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
