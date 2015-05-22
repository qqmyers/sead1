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
package edu.illinois.ncsa.mmdb.web.server.dispatch;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.UriRef;
import org.tupeloproject.rdf.terms.Cet;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.DcTerms;
import org.tupeloproject.rdf.terms.Files;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.rdf.terms.Tags;
import org.tupeloproject.util.Tuple;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQuery;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult.ListQueryItem;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListQueryResult.ListQueryItem.SectionHit;
import edu.illinois.ncsa.mmdb.web.client.dispatch.SearchResult;
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.illinois.ncsa.mmdb.web.server.search.SearchHandler;
import edu.illinois.ncsa.mmdb.web.server.search.SearchWithFilterHandler;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.mmdb.MMDB;

/**
 * ListQueryHandler retrieves lists of items (datasets or collections) based on
 * a number of metadata and search criteria. It handles basic queries to find
 * top-level items, items in a collection or with a given tag, and, via other
 * classes, metadata and free-text based searches. Results may be filtered to
 * list
 * only datasets or collections, and deleted items and those not accessible to a
 * given user are automatically removed from the returned lists.
 * Sort options and list size offsets and limits can also be specified.
 *
 * @author Rob Kooper
 * @author myersjd@umich.edu
 */
public class ListQueryHandler implements ActionHandler<ListQuery, ListQueryResult> {

    /** Commons logging **/
    private static Log           log = LogFactory.getLog(ListQueryHandler.class);
    private final PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());

    @Override
    public ListQueryResult execute(ListQuery listquery, ExecutionContext context) throws ActionException {
        ListQueryResult queryResult = new ListQueryResult();
        List<ListQueryItem> resultList;

        long startTime = System.currentTimeMillis();

        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        int userlevel = rbac.getUserAccessLevel(Resource.uriRef(listquery.getUser()));
        int defaultlevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));

        /* Handle Search - with or without predicate filter */
        SearchResult sr = null;
        if (listquery.getFilter() != null) {
            sr = new SearchWithFilterHandler().performQuery(listquery.getSearchTerm(), listquery.getFilter());

        } else if (listquery.getSearchTerm() != null) {
            sr = new SearchHandler().performQuery(listquery.getSearchTerm());
        }
        /*If a search, package hits as a set of items with hits*/
        if (sr != null) {
            //Turn hits into a list of items
            Map<String, ListQueryItem> hitMap = getHitItems(sr, listquery.getUser(), userlevel, defaultlevel);
            log.debug("hitmap has " + hitMap.size() + " entries");
            // fill in result
            resultList = new ArrayList<ListQueryItem>();
            int count = 0;
            for (java.util.Map.Entry<String, ListQueryItem> e : hitMap.entrySet() ) {
                addQueryItem(e.getValue(), listquery, resultList);
                count++;
            }
            queryResult.setTotalCount(count);
            queryResult.setResults(trimResultList(resultList, listquery.getOffset(), listquery.getLimit()));

        } else {
            //Do Normal ListQuery for matching items
            log.debug("offset and limit: " + listquery.getOffset() + " " + listquery.getLimit());
            performQuery(queryResult, listquery, userlevel, defaultlevel);
        }

        log.info("Items fetch results : " + queryResult.getTotalCount() + " in " + (System.currentTimeMillis() - startTime) + " ms");
        return queryResult;

    }

    private List<ListQueryItem> trimResultList(List<ListQueryItem> resultList, int offset, int limit) {
        List<ListQueryItem> finalList = new ArrayList<ListQueryItem>();
        for (int i = offset; i < offset + limit && i < resultList.size(); i++ ) {
            finalList.add(resultList.get(i));
        }
        return finalList;
    }

    //Fills in queryResult
    private void performQuery(ListQueryResult queryResult, ListQuery listquery, int userlevel, int defaultlevel) throws ActionException {

        Unifier u = new Unifier();

        // some simple limitations
        if (listquery.getCollection() != null) {
            u.addPattern(Resource.uriRef(listquery.getCollection()), DcTerms.HAS_PART, "s");
        }
        if (listquery.getTag() != null) {
            u.addPattern("s", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(listquery.getTag()));
        }
        if (listquery.getBean() != null) {
            u.addPattern("s", Rdf.TYPE, Resource.uriRef(listquery.getBean()));
        }

        // add all items we might need
        u.addColumnName("s"); // 0
        u.addPattern("s", Rdf.TYPE, "t");
        u.addColumnName("t"); // 1
        u.addPattern("s", Dc.DATE, "d1", true);
        u.addColumnName("d1"); // 2
        u.addPattern("s", DcTerms.DATE_CREATED, "d2", true);
        u.addColumnName("d2"); // 3
        u.addPattern("s", Dc.TITLE, "n");
        u.addColumnName("n"); // 4
        u.addPattern("s", Dc.CREATOR, "a");
        u.addColumnName("a"); // 5
        u.addPattern("s", Files.LENGTH, "l", true);
        u.addColumnName("l"); // 6
        u.addPattern("s", Dc.FORMAT, "f", true);
        u.addColumnName("f"); // 7
        String pred = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
        u.addPattern("s", Resource.uriRef(pred), "r", true);
        u.addColumnName("r"); // 8
        u.addPattern("s", Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "k", true);
        u.addColumnName("k"); // 9

        //if we are looking for top level items (Flag is true and no parent collection set)
        if ((listquery.getShowDataLevel() == true) && (listquery.getCollection() == null)) {
            u.addPattern(AddToCollectionHandler.TOP_LEVEL, AddToCollectionHandler.INCLUDES, "s");
        }
        // fetch results
        Set<String> keys = new HashSet<String>();
        List<ListQueryItem> resultList = new ArrayList<ListQueryItem>();
        int count = 0;
        try {
            TupeloStore.getInstance().getContext().perform(u);

            for (Tuple<Resource> row : u.getResult() ) {
                // skip wrong beans
                if (listquery.getBean() == null) {
                    if (!row.get(1).equals(Cet.DATASET) && !row.get(1).equals(CollectionBeanUtil.COLLECTION_TYPE)) {
                        continue;
                    }
                } else if (!listquery.getBean().equals(row.get(1).getString())) {
                    continue;
                }

                if (keys.contains(row.get(0).getString())) {
                    //Happens if a metadata field have multiple values when it shouldn't (given the items were asking for)
                    log.warn("Already contains item for " + row);
                    continue;
                }
                keys.add(row.get(0).getString());

                // skip deleted items
                if (row.get(9) != null) {
                    continue;
                }

                // skip items user can not see
                if (!CollectionBeanUtil.COLLECTION_TYPE.equals(row.get(1)) && !row.get(5).getString().equals(listquery.getUser())) {
                    int datasetlevel = (row.get(8) != null) ? Integer.parseInt(row.get(8).getString()) : defaultlevel;
                    if (datasetlevel < userlevel) {
                        continue;
                    }
                }
                // all items are ok from here forward
                count++;
                // create the item
                ListQueryItem item = new ListQueryItem();

                item.setUri(row.get(0).getString());
                item.setTitle(row.get(4).getString());
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
                if (row.get(6) != null) {
                    if (row.get(6).asObject() instanceof Long) {
                        item.setSize(humanBytes((Long) row.get(6).asObject()));
                    } else {
                        item.setSize(row.get(6).getString());
                    }
                }
                if (row.get(7) != null) {
                    item.setCategory(TupeloStore.getInstance().getMimeMap().getCategory(row.get(7).getString()));
                }
                if (item.getCategory() == null) {
                    if (CollectionBeanUtil.COLLECTION_TYPE.getString().equals(row.get(1).getString())) {
                        item.setCategory("Collection");
                    } else {
                        item.setCategory("Unknown");
                    }
                }
                addQueryItem(item, listquery, resultList);
            }
        } catch (OperatorException exc) {
            log.error("Could not fetch items.", exc);
            throw new ActionException("Could not get items from tupelo.", exc);
        }
        queryResult.setTotalCount(count);
        queryResult.setResults(trimResultList(resultList, listquery.getOffset(), listquery.getLimit()));
    }

    private void addQueryItem(ListQueryItem item, ListQuery listquery, List<ListQueryItem> resultList) {
        // find location in list
        if (resultList.size() == 0) {
            resultList.add(item);
        } else {
            int location = 0;
            while ((location < resultList.size()) && compare(item, resultList.get(location), listquery) >= 0) {
                location++;
            }
            if (location <= listquery.getOffset() + listquery.getLimit()) {
                if (location > resultList.size()) {
                    resultList.add(item);
                } else {
                    resultList.add(location, item);
                }
                if (resultList.size() > (listquery.getOffset() + listquery.getLimit())) {
                    resultList.remove(resultList.size() - 1);
                }
            }
        }

    }

    /**
     * Get the set of items (datasets or collections) associated with hits
     * (which may be to sections of documents).
     *
     * @param sr
     *            - the list of hits
     * @param user
     *            - the current user - used to check access control
     * @param userlevel
     *            - the user's access level
     * @param defaultlevel
     *            - the default level to assume for items that don't have one
     *            set
     * @return - a map of item ids (uri strings) to ListQueryItems with item
     *         info and a list of associated hits
     * @throws ActionException
     */
    private Map<String, ListQueryItem> getHitItems(SearchResult sr, String user, int userlevel, int defaultlevel) throws ActionException {
        Map<String, ListQueryItem> hits = new HashMap<String, ListQueryItem>();
        for (String s : sr.getHits() ) {
            log.debug("Hit uri is: " + s);
            // get the type of the hit
            Unifier u = new Unifier();
            u.setColumnNames("type");
            UriRef item = Resource.uriRef(s);
            u.addPattern(item, Rdf.TYPE, "type");
            try {
                TupeloStore.getInstance().getContext().perform(u);
            } catch (OperatorException e1) {
                log.error("Operator exception getting search hit: ", e1);
            }
            boolean isCollection = false;
            String uri = s;
            String sectionUri = null;
            String sectionLabel = null;
            String sectionMarker = null;
            Set<String> restrictedUris = new HashSet<String>();

            for (Tuple<Resource> row : u.getResult() ) {

                Resource type = row.get(0);
                log.debug("Hit of type: " + type.getString());
                if (Cet.DATASET.equals(type)) {
                    //Done - have the right uri
                    break;
                } else if (CollectionBeanUtil.COLLECTION_TYPE.equals(type)) {
                    //uri is OK
                    isCollection = true;
                    break;
                } else if (MMDB.SECTION_TYPE.equals(type)) {
                    // get section info
                    log.trace("looking for parent item");
                    Unifier us = new Unifier();
                    us.setColumnNames("dataset", "label", "marker");

                    us.addPattern("dataset", MMDB.METADATA_HASSECTION, item);
                    us.addPattern(item, MMDB.SECTION_LABEL, "label");
                    us.addPattern(item, MMDB.SECTION_MARKER, "marker");
                    try {
                        TupeloStore.getInstance().getContext().perform(us);
                    } catch (OperatorException e) {
                        log.error("Error querying for information about section of dataset " + s, e);
                    }
                    for (Tuple<Resource> row2 : us.getResult() ) {
                        //Get uri of item with tag
                        uri = row2.get(0).getString();
                        //record section info
                        sectionUri = s;
                        sectionLabel = row2.get(1).getString();
                        sectionMarker = row2.get(2).getString();
                        log.trace("Found: " + uri + " " + sectionLabel + " " + sectionMarker);

                    }
                    break;
                }
            }
            log.debug("Item uri for hit is: " + uri);
            //we haven't processed this uri before...
            if (!hits.containsKey(uri) && !restrictedUris.contains(uri)) {
                ListQueryItem lqItem = null;

                Unifier u1 = new Unifier();
                UriRef sRef = Resource.uriRef(uri);
                // add all items we might need
                u1.addPattern(sRef, Dc.TITLE, "n");
                u1.addColumnName("n"); // 0
                u1.addPattern(sRef, Dc.DATE, "d1", true);
                u1.addColumnName("d1"); // 1
                u1.addPattern(sRef, DcTerms.DATE_CREATED, "d2", true);
                u1.addColumnName("d2"); // 2
                u1.addPattern(sRef, Dc.CREATOR, "a");
                u1.addColumnName("a"); // 3
                u1.addPattern(sRef, Files.LENGTH, "l", true);
                u1.addColumnName("l"); // 4
                u1.addPattern(sRef, Dc.FORMAT, "f", true);
                u1.addColumnName("f"); // 5
                String pred = TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelPredicate);
                u1.addPattern(sRef, Resource.uriRef(pred), "r", true);
                u1.addColumnName("r"); // 6
                u1.addPattern(sRef, Resource.uriRef("http://purl.org/dc/terms/isReplacedBy"), "k", true);
                u1.addColumnName("k"); // 7
                try {
                    TupeloStore.getInstance().getContext().perform(u1);

                    for (Tuple<Resource> row1 : u1.getResult() ) {

                        // skip deleted items
                        if (row1.get(7) != null) {
                            log.trace("skipping deleted");
                            continue;
                        }

                        // skip items user can not see
                        if (!isCollection && !row1.get(3).getString().equals(user)) {
                            int datasetlevel = (row1.get(6) != null) ? Integer.parseInt(row1.get(6).getString()) : defaultlevel;
                            if (datasetlevel < userlevel) {
                                log.trace("skipping access-controlled item:" + uri);
                                restrictedUris.add(uri);
                                continue;
                            }
                        }

                        // create the item
                        lqItem = new ListQueryItem();
                        lqItem.setUri(uri);
                        lqItem.setTitle(row1.get(0).getString());
                        lqItem.setAuthor(pbu.get(row1.get(3)).getName());
                        if (row1.get(1) != null) {
                            if (row1.get(1).asObject() instanceof Date) {
                                lqItem.setDate((Date) row1.get(1).asObject());
                            }
                        } else {
                            if (row1.get(2).asObject() instanceof Date) {
                                lqItem.setDate((Date) row1.get(2).asObject());
                            }
                        }
                        if (row1.get(4) != null) {
                            if (row1.get(4).asObject() instanceof Long) {
                                lqItem.setSize(humanBytes((Long) row1.get(4).asObject()));
                            } else {
                                lqItem.setSize(row1.get(4).getString());
                            }
                        }
                        if (row1.get(5) != null) {
                            lqItem.setCategory(TupeloStore.getInstance().getMimeMap().getCategory(row1.get(5).getString()));
                        }
                        if (lqItem.getCategory() == null) {
                            if (isCollection) {
                                lqItem.setCategory("Collection");
                            } else {
                                lqItem.setCategory("Unknown");
                            }
                        }
                        if (sectionUri != null) {
                            List<SectionHit> hitList = new ArrayList<SectionHit>();
                            hitList.add(new SectionHit(sectionUri, sectionLabel, sectionMarker));
                            lqItem.setHits(hitList);
                        }
                    }

                } catch (OperatorException exc) {
                    log.error("Could not fetch items.", exc);
                    throw new ActionException("Could not get items from tupelo.", exc);
                }
                if (lqItem != null) {
                    hits.put(uri, lqItem);
                }
            } else if (hits.containsKey(uri)) {
                //We've already added a hit(s) for this uri so just add the current hit
                List<SectionHit> theList = hits.get(uri).getHits();
                if (theList == null) {
                    theList = new ArrayList<SectionHit>();
                    hits.get(uri).setHits(theList);
                }
                theList.add(new SectionHit(sectionUri, sectionLabel, sectionMarker));
            }
        }
        return hits;
    }

    /**
     * @param item1
     *            the first item to be compared.
     * @param item2
     *            the second item to be compared.
     * @param listquery
     *            how to compare
     * @return the value <code>0</code> if the argument item1 is equal to
     *         item2; a value less than <code>0</code> if item1
     *         is lexicographically less than item2; and a
     *         value greater than <code>0</code> if item1 is
     *         lexicographically greater than item2.
     */
    public int compare(ListQueryItem item1, ListQueryItem item2, ListQuery listquery) {
        if (item1 == null) {
            return +1;
        }
        if (item2 == null) {
            return -1;
        }

        // translate orderBy to the right sort
        if (listquery.getOrderBy().equals("date-asc")) {
            if (item1.getDate() == null) {
                return +1;
            }
            if (item2.getDate() == null) {
                return -1;
            }
            return item1.getDate().compareTo(item2.getDate());
        } else if (listquery.getOrderBy().equals("date-desc")) {
            if (item1.getDate() == null) {
                return +1;
            }
            if (item2.getDate() == null) {
                return -1;
            }
            return -item1.getDate().compareTo(item2.getDate());
        } else if (listquery.getOrderBy().equals("title-asc")) {
            if (item1.getTitle() == null) {
                return +1;
            }
            if (item2.getTitle() == null) {
                return -1;
            }
            if (item1.getTitle().equalsIgnoreCase(item2.getTitle())) {
                if (item1.getDate() == null) {
                    return +1;
                }
                if (item2.getDate() == null) {
                    return -1;
                }
                return item1.getDate().compareTo(item2.getDate());
            } else {
                return item1.getTitle().compareToIgnoreCase(item2.getTitle());
            }
        } else if (listquery.getOrderBy().equals("title-desc")) {
            if (item1.getTitle() == null) {
                return +1;
            }
            if (item2.getTitle() == null) {
                return -1;
            }
            if (item1.getTitle().equalsIgnoreCase(item2.getTitle())) {
                if (item1.getDate() == null) {
                    return +1;
                }
                if (item2.getDate() == null) {
                    return -1;
                }
                return item1.getDate().compareTo(item2.getDate());
            } else {
                return -item1.getTitle().compareToIgnoreCase(item2.getTitle());
            }
        } else if (listquery.getOrderBy().equals("category-asc")) {
            if (item1.getCategory() == null) {
                return +1;
            }
            if (item2.getCategory() == null) {
                return -1;
            }
            if (item1.getCategory().equalsIgnoreCase(item2.getCategory())) {
                if (item1.getDate() == null) {
                    return +1;
                }
                if (item2.getDate() == null) {
                    return -1;
                }
                return item1.getDate().compareTo(item2.getDate());
            } else {
                return item1.getCategory().compareToIgnoreCase(item2.getCategory());
            }
        } else if (listquery.getOrderBy().equals("category-desc")) {
            if (item1.getCategory() == null) {
                return +1;
            }
            if (item2.getCategory() == null) {
                return -1;
            }
            if (item1.getCategory().equalsIgnoreCase(item2.getCategory())) {
                if (item1.getDate() == null) {
                    return +1;
                }
                if (item2.getDate() == null) {
                    return -1;
                }
                return item1.getDate().compareTo(item2.getDate());
            } else {
                return -item1.getCategory().compareToIgnoreCase(item2.getCategory());
            }
        } else {
            if (item1.getDate() == null) {
                return -1;
            }
            if (item2.getDate() == null) {
                return +1;
            }
            return -item1.getDate().compareTo(item2.getDate());
        }
    }

    private String humanBytes(long x) {
        if (x == Integer.MAX_VALUE) {
            return "No limit";
        }
        if (x < -3) {
            x += Math.pow(2, 32);
        } else if (x < 0) {
            x = 0;
        }
        if (x < 1e3) {
            return x + " bytes";
        } else if (x < 1e6) {
            return (int) (x / 1e3 * 100) / 100.0 + " KB";
        } else if (x < 1e9) {
            return (int) (x / 1e6 * 100) / 100.0 + " MB";
        } else if (x < 1e12) {
            return (int) (x / 1e9 * 100) / 100.0 + " GB";
        } else if (x < 1e15) {
            return (int) (x / 1e12 * 100) / 100.0 + " TB";
        } else {
            return x + " bytes";
        }
    }

    @Override
    public Class<ListQuery> getActionType() {
        return ListQuery.class;
    }

    @Override
    public void rollback(ListQuery arg0, ListQueryResult arg1, ExecutionContext arg2) throws ActionException {
        // TODO Auto-generated method stub

    }

}
