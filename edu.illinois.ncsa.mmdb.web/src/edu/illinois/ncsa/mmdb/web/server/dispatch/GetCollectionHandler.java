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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.ObjectResourceMapping;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollection;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetCollectionResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.GetPreviews;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoCollectionBean;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoPointBean;
import edu.illinois.ncsa.mmdb.web.client.ui.preview.PreviewGeoserverCollectionBean;
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
 * 
 */
public class GetCollectionHandler implements
        ActionHandler<GetCollection, GetCollectionResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(GetCollectionHandler.class);

    @Override
    public GetCollectionResult execute(GetCollection arg0, ExecutionContext arg1)
            throws ActionException {

        BeanSession beanSession = TupeloStore.getInstance().getBeanSession();

        CollectionBeanUtil cbu = new CollectionBeanUtil(beanSession);

        try {
            CollectionBean collectionBean = fastGetCollection(cbu, arg0.getUri());

            // FIXME this might be slow for large collections, although its result is memoized per collection
            int collectionSize = TupeloStore.getInstance().countDatasets(arg0.getUri(), false);

            GetCollectionResult result = new GetCollectionResult(collectionBean, collectionSize);
            result.setPreviews(getCollectionPreviews(arg0.getUri()));

            return result;
        } catch (Exception e) {
            throw new ActionException(e);
        }
    }

    // FIXME use Rob's BeanFactory instead of this hardcoded way
    private CollectionBean fastGetCollection(CollectionBeanUtil cbu, String uriString) throws OperatorException {
        PersonBeanUtil pbu = new PersonBeanUtil(cbu.getBeanSession());
        Unifier u = new Unifier();
        Resource uri = Resource.uriRef(uriString);
        u.addPattern(uri, Rdf.TYPE, cbu.getType());
        u.addPattern(uri, Dc.CREATOR, "creator", true);
        u.addPattern(uri, Dc.TITLE, "title", true);
        u.addPattern(uri, Dc.DESCRIPTION, "description", true);
        u.addPattern(uri, DcTerms.DATE_CREATED, "dateCreated", true);
        u.addPattern(uri, DcTerms.DATE_MODIFIED, "dateModified", true);
        u.setColumnNames("creator", "title", "description", "dateCreated", "dateModified");
        CollectionBean colBean = new CollectionBean();
        TupeloStore.getInstance().getContext().perform(u);
        for (Tuple<Resource> row : u.getResult() ) {
            log.debug(row);
            int r = 0;
            Resource creator = row.get(r++);
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

    private Collection<PreviewBean> getCollectionPreviews(String collectionUri) {
        ArrayList<PreviewBean> previews = new ArrayList<PreviewBean>();
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
        List<String> datasetUris = ListDatasetsHandler.listDatasetUris(Dc.DATE.getString(), false, 0, 0, collectionUri, null, dbu);

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

        return previews;

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
