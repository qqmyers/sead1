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
import java.util.HashSet;
import java.util.List;
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
import edu.illinois.ncsa.mmdb.web.common.ConfigurationKey;
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
        long l = System.currentTimeMillis();

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

        int parentIndex = -1;
        //if we are looking for top level collections
        if (listquery.getBean() != null && listquery.getCollection() == null && (CollectionBeanUtil.COLLECTION_TYPE.toString().equals(listquery.getBean()) || listquery.getShowDataLevel() == true)) {
            u.addColumnName("parent"); //10
            u.addPattern("parent", DcTerms.HAS_PART, "s", true);
            List<String> names = u.getColumnNames();
            parentIndex = names.indexOf("parent");
        }
        // fetch results
        Set<String> keys = new HashSet<String>();
        List<ListQueryItem> result = new ArrayList<ListQueryItem>();
        PersonBeanUtil pbu = new PersonBeanUtil(TupeloStore.getInstance().getBeanSession());
        SEADRbac rbac = new SEADRbac(TupeloStore.getInstance().getContext());
        int userlevel = rbac.getUserAccessLevel(Resource.uriRef(listquery.getUser()));
        int defaultlevel = Integer.parseInt(TupeloStore.getInstance().getConfiguration(ConfigurationKey.AccessLevelDefault));
        try {
            TupeloStore.getInstance().getContext().perform(u);
            int count = 0;
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
                    log.warn("Already contain item for " + row);
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
                //skip items if we are looking for top level collections
                if (parentIndex >= 0 && row.get(parentIndex) != null) {
                    continue;
                }
                // all items are ok from here forward
                count++;

                // create the item
                ListQueryItem item = new ListQueryItem();

                item.setUri(row.get(0).getString());
                item.setTitle(row.get(4).getString());
                item.setAuthor(pbu.get((UriRef) row.get(5)).getName());
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

                // find location in list
                if (result.size() == 0) {
                    result.add(item);
                } else {
                    int location = 0;
                    while ((location < result.size()) && compare(item, result.get(location), listquery) >= 0) {
                        location++;
                    }
                    if (location <= listquery.getOffset() + listquery.getLimit()) {
                        if (location > result.size()) {
                            result.add(item);
                        } else {
                            result.add(location, item);
                        }
                        if (result.size() > (listquery.getOffset() + listquery.getLimit())) {
                            result.remove(result.size() - 1);
                        }
                    }
                }
            }

            // fill in result
            queryResult.setTotalCount(count);
            queryResult.setResults(new ArrayList<ListQueryItem>());
            for (int i = listquery.getOffset(); i < listquery.getOffset() + listquery.getLimit() && i < result.size(); i++ ) {
                queryResult.getResults().add(result.get(i));
            }

            log.info("Items fetch results : " + (System.currentTimeMillis() - l));
            return queryResult;
        } catch (OperatorException exc) {
            log.error("Could not fetch items.", exc);
            throw new ActionException("Could not get items from tupelo.", exc);
        }
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
