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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.tupeloproject.kernel.BeanSession;
import org.tupeloproject.kernel.OperatorException;
import org.tupeloproject.kernel.Unifier;
import org.tupeloproject.rdf.Resource;
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
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
import edu.illinois.ncsa.mmdb.web.server.DatasourceBeanPreprocessor;
import edu.illinois.ncsa.mmdb.web.server.SEADRbac;
import edu.illinois.ncsa.mmdb.web.server.TupeloStore;
import edu.uiuc.ncsa.cet.bean.tupelo.CollectionBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.PersonBeanUtil;
import edu.uiuc.ncsa.cet.bean.tupelo.TagEventBeanUtil;

/**
 * TODO Add comments
 * 
 * @author Rob Kooepr
 * 
 */
public class ListQueryHandler implements ActionHandler<ListQuery, ListQueryResult> {

    /** Commons logging **/
    private static Log log = LogFactory.getLog(ListQueryHandler.class);

    @Override
    public ListQueryResult execute(ListQuery listquery, ExecutionContext context) throws ActionException {
        ListQueryResult queryResult = new ListQueryResult();
        queryResult.setResults(new ArrayList<ListQueryItem>());

        long l = System.currentTimeMillis();
        //        if (TupeloStore.getInstance().useDatasetTable() && (listquery.getCollection() == null) && (listquery.getTag() == null) && (Cet.DATASET.getString().equals(listquery.getBean()))) {
        //            getDatasetUsingTable(listquery, queryResult);
        //        } else {
        getItemsTupelo(listquery, queryResult);
        //        }
        log.info("Items fetch results : " + (System.currentTimeMillis() - l));

        return queryResult;
    }

    private void getDatasetUsingTable(ListQuery listquery, ListQueryResult queryResult) throws ActionException {
        try {
            BeanSession beanSession = TupeloStore.getInstance().getBeanSession();
            DatasourceBeanPreprocessor proc = (DatasourceBeanPreprocessor) beanSession.getBeanPreprocessor();
            Connection connection = proc.getDataSource().getConnection();
            Statement statement = connection.createStatement();

            String query = "SELECT * FROM `dataset` ";

            if (listquery.getOrderBy().equals("date-desc")) {
                query += " ORDER BY date DESC";
            } else if (listquery.getOrderBy().equals("date-asc")) {
                query += " ORDER BY date ASC";
            } else if (listquery.getOrderBy().equals("title-desc")) {
                query += " ORDER BY title DESC";
            } else if (listquery.getOrderBy().equals("title-asc")) {
                query += " ORDER BY title ASC";
            } else if (listquery.getOrderBy().equals("category-desc")) {
                query += " ORDER BY category DESC";
            } else if (listquery.getOrderBy().equals("category-asc")) {
                query += " ORDER BY category ASC";
            } else {
                query += " ORDER BY date DESC";
            }

            if (listquery.getOffset() > 0) {
                query += " LIMIT " + listquery.getOffset();
                if (listquery.getLimit() > 0) {
                    query += ", " + listquery.getLimit();
                }
            } else if (listquery.getLimit() > 0) {
                query += " LIMIT 0, " + listquery.getLimit();
            }

            List<ListQueryItem> items = new ArrayList<ListQueryItem>();
            PersonBeanUtil pbu = new PersonBeanUtil(beanSession);

            ResultSet resultset = statement.executeQuery(query);
            while (resultset.next()) {
                ListQueryItem item = new ListQueryItem();
                item.setUri(resultset.getString("uri"));
                item.setTitle(resultset.getString("title"));
                item.setAuthor(pbu.get(resultset.getString("creator")).getName());
                item.setDate(resultset.getTimestamp("date"));
                item.setSize(humanBytes(resultset.getLong("size")));
                item.setCategory(TupeloStore.getInstance().getMimeMap().getCategory(resultset.getString("mimetype")));
                items.add(item);
            }
            resultset.close();
            queryResult.setResults(items);

            resultset = statement.executeQuery("SELECT COUNT(*) FROM `dataset`;");
            if (resultset.next()) {
                queryResult.setTotalCount(resultset.getInt(1));
            }
            resultset.close();

            statement.close();
            connection.close();
        } catch (Exception exc) {
            log.error("Could not get datasets from dataset table.", exc);
            throw new ActionException("Could not get datasets from dataset table.", exc);
        }
    }

    private void getItemsTupelo(final ListQuery listquery, ListQueryResult queryResult) throws ActionException {
        Unifier u = new Unifier();
        if (listquery.getCollection() != null) {
            u.addPattern(Resource.uriRef(listquery.getCollection()), DcTerms.HAS_PART, "s");
        }
        if (listquery.getTag() != null) {
            u.addPattern("s", Tags.TAGGED_WITH_TAG, TagEventBeanUtil.createTagUri(listquery.getTag()));
        }
        if (listquery.getBean() != null) {
            u.addPattern("s", Rdf.TYPE, Resource.uriRef(listquery.getBean()));
        }

        sortMap(createMap(u, listquery), listquery, queryResult);
    }

    private Map<String, ListQueryItem> createMap(Unifier u, ListQuery listquery) throws ActionException {
        // add all items we might need
        u.addColumnName("s");
        u.addPattern("s", Rdf.TYPE, "t");
        u.addColumnName("t");
        u.addPattern("s", Dc.DATE, "d1", true);
        u.addColumnName("d1");
        u.addPattern("s", DcTerms.DATE_CREATED, "d2", true);
        u.addColumnName("d2");
        u.addPattern("s", Dc.TITLE, "n");
        u.addColumnName("n");
        u.addPattern("s", Dc.CREATOR, "a");
        u.addColumnName("a");
        u.addPattern("s", Files.LENGTH, "l", true);
        u.addColumnName("l");
        u.addPattern("s", Dc.FORMAT, "f", true);
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
        Map<String, ListQueryItem> map = new HashMap<String, ListQueryItem>();
        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        int userlevel = rbac.getUserAccessLevel(Resource.uriRef(listquery.getUser()));
        int defaultlevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));
        try {
            for (Tuple<Resource> row : TupeloStore.getInstance().unifyExcludeDeleted(u, "s") ) {
                if ("tag:tupeloproject.org,2006:/2.0/beans/2.0/storageTypeBeanEntry".equals(row.get(1).getString())) {
                    continue;
                }
                if (map.containsKey(row.get(0).getString())) {
                    log.warn("Already contain item for " + row);
                    continue;
                }
                if (Cet.DATASET.equals(row.get(1)) && !row.get(5).getString().equals(listquery.getUser())) {
                    int datasetlevel = (row.get(8) != null) ? Integer.parseInt(row.get(8).getString()) : defaultlevel;
                    if (datasetlevel < userlevel) {
                        continue;
                    }
                }
                ListQueryItem item = new ListQueryItem();
                map.put(row.get(0).getString(), item);

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
            }

            return map;
        } catch (OperatorException exc) {
            log.error("Could not fetch items.", exc);
            throw new ActionException("Could not get items from tupelo.", exc);
        }

    }

    private void sortMap(final Map<String, ListQueryItem> map, final ListQuery listquery, ListQueryResult queryResult) {
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

                // translate orderBy to the right sort
                if (listquery.getOrderBy().equals("date-asc")) {
                    if (item1.getDate() == null) {
                        return -1;
                    }
                    if (item2.getDate() == null) {
                        return +1;
                    }
                    return item1.getDate().compareTo(item2.getDate());
                } else if (listquery.getOrderBy().equals("date-desc")) {
                    if (item1.getDate() == null) {
                        return -1;
                    }
                    if (item2.getDate() == null) {
                        return +1;
                    }
                    return -item1.getDate().compareTo(item2.getDate());
                } else if (listquery.getOrderBy().equals("title-asc")) {
                    if (item1.getTitle() == null) {
                        return -1;
                    }
                    if (item2.getTitle() == null) {
                        return +1;
                    }
                    if (item1.getTitle().equalsIgnoreCase(item2.getTitle())) {
                        if (item1.getDate() == null) {
                            return -1;
                        }
                        if (item2.getDate() == null) {
                            return +1;
                        }
                        return -item1.getDate().compareTo(item2.getDate());
                    } else {
                        return item1.getTitle().compareToIgnoreCase(item2.getTitle());
                    }
                } else if (listquery.getOrderBy().equals("title-desc")) {
                    if (item1.getTitle() == null) {
                        return -1;
                    }
                    if (item2.getTitle() == null) {
                        return +1;
                    }
                    if (item1.getTitle().equalsIgnoreCase(item2.getTitle())) {
                        if (item1.getDate() == null) {
                            return -1;
                        }
                        if (item2.getDate() == null) {
                            return +1;
                        }
                        return -item1.getDate().compareTo(item2.getDate());
                    } else {
                        return -item1.getTitle().compareToIgnoreCase(item2.getTitle());
                    }
                } else if (listquery.getOrderBy().equals("category-asc")) {
                    if (item1.getCategory() == null) {
                        return -1;
                    }
                    if (item2.getCategory() == null) {
                        return +1;
                    }
                    if (item1.getCategory().equals(item2.getCategory())) {
                        if (item1.getDate() == null) {
                            return -1;
                        }
                        if (item2.getDate() == null) {
                            return +1;
                        }
                        return -item1.getDate().compareTo(item2.getDate());
                    } else {
                        return item1.getCategory().compareTo(item2.getCategory());
                    }
                } else if (listquery.getOrderBy().equals("category-desc")) {
                    if (item1.getCategory() == null) {
                        return -1;
                    }
                    if (item2.getCategory() == null) {
                        return +1;
                    }
                    if (item1.getCategory().equals(item2.getCategory())) {
                        if (item1.getDate() == null) {
                            return -1;
                        }
                        if (item2.getDate() == null) {
                            return +1;
                        }
                        return -item1.getDate().compareTo(item2.getDate());
                    } else {
                        return -item1.getCategory().compareTo(item2.getCategory());
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
        });
        queryResult.setTotalCount(uris.size());
        uris = uris.subList(listquery.getOffset(), Math.min(uris.size(), listquery.getOffset() + listquery.getLimit()));
        for (String uri : uris ) {
            queryResult.getResults().add(map.get(uri));
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
