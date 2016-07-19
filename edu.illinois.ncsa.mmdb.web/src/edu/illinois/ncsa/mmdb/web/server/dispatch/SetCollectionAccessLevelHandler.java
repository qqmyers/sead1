/*******************************************************************************
 * Copyright 2016 University of Michigan

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 *******************************************************************************/
/**
 *
 */
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.Context;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Table;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.GetAccessLevelResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SetCollectionAccessLevel;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.common.Permission;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.rbac.RBACException;

/**
 * Set access level for a collection, recursing through all sub-collections and
 * child datasets
 * Restricted to admins since it is a potentially large operation and will
 * replace any existing access settings on children
 *
 * @author myersjd@umich.edu
 *
 */
public class SetCollectionAccessLevelHandler implements ActionHandler<SetCollectionAccessLevel, GetAccessLevelResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(SetCollectionAccessLevelHandler.class);

    @Override
    public GetAccessLevelResult execute(SetCollectionAccessLevel arg0, ExecutionContext arg1) throws ActionException {
        Context context = TupeloStore.getInstance().getContext();
        SEADRbac rbac = new SEADRbac(context);
        try {
            if (rbac.checkPermission(arg0.getUser(), Permission.VIEW_ADMIN_PAGES)) {
                try {
                    Resource pred = Resource.uriRef(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate));
                    UriRef parent = Resource.uriRef(arg0.getUri());
                    ArrayList<UriRef> uriRefs = new ArrayList<UriRef>(100);
                    uriRefs.addAll(getDatasets(parent));

                    int defaultLevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));
                    int numLevels = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelValues).split(",").length;
                    int newLevel = arg0.getLevel();
                    if (newLevel >= numLevels) {
                        newLevel = -1; //don't set
                    }

                    rbac.bulkSetAccessLevel(uriRefs, pred, newLevel);
                } catch (OperatorException x) {
                    throw new ActionException("failed to set access level on " + arg0.getUri(), x);
                }
            } else {
                log.debug("no permission to set access level on " + arg0.getUri());
            }
        } catch (RBACException x) {
            throw new ActionException("failed to check set metadata permission", x);
        }
        return GetAccessLevelHandler.getResult(arg0.getUri());
    }

    private Collection<UriRef> getDatasets(UriRef parent) throws OperatorException {
        ArrayList<UriRef> subColDatasetsArrayList = new ArrayList<UriRef>();
        Unifier dataUnifier = new Unifier();
        dataUnifier.addPattern(parent, DcTerms.HAS_PART, "dataset");
        dataUnifier.addPattern("dataset", Rdf.TYPE, Cet.DATASET);
        dataUnifier.addColumnName("dataset");
        Table<Resource> datasetsTable = TupeloStore.getInstance().unifyExcludeDeleted(dataUnifier, "dataset");
        Iterator<Tuple<Resource>> iter = datasetsTable.iterator();
        while (iter.hasNext()) {
            subColDatasetsArrayList.add((UriRef) iter.next().get(0));
        }
        Unifier subColUnifier = new Unifier();
        subColUnifier.addPattern(parent, DcTerms.HAS_PART, "subCol");
        subColUnifier.addPattern("subCol", Rdf.TYPE, CollectionBeanUtil.COLLECTION_TYPE);
        subColUnifier.addColumnName("subCol");
        Table<Resource> subColsTable = TupeloStore.getInstance().unifyExcludeDeleted(subColUnifier, "subCol");
        iter = subColsTable.iterator();
        while (iter.hasNext()) {
            subColDatasetsArrayList.addAll(getDatasets((UriRef) iter.next().get(0)));
        }
        return subColDatasetsArrayList;
    }

    @Override
    public Class<SetCollectionAccessLevel> getActionType() {
        return SetCollectionAccessLevel.class;
    }

    @Override
    public void rollback(SetCollectionAccessLevel arg0, GetAccessLevelResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub
    }

}
