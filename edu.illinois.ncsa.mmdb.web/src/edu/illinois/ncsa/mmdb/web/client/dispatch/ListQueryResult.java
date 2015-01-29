/*******************************************************************************
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2010 , NCSA.  All rights reserved.
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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

/**
 * @author lmarini
 * @author myersjd@umich.edu
 *
 */
@SuppressWarnings("serial")
public class ListQueryResult implements Result {

    private List<ListQueryItem> results;
    private int                 totalCount;

    public ListQueryResult() {
    }

    public ListQueryResult(List<ListQueryItem> result) {
        setResults(result);
    }

    public void setResults(List<ListQueryItem> results) {
        this.results = results;
    }

    public List<ListQueryItem> getResults() {
        return results;
    }

    public void setTotalCount(int count) {
        this.totalCount = count;
    }

    public int getTotalCount() {
        return totalCount;
    }

    static public class ListQueryItem implements Serializable {
        private String           uri;
        private String           title;
        private String           author;
        private Date             date;
        private String           size;
        private String           category;
        private List<SectionHit> hits = null;

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public List<SectionHit> getHits() {
            return hits;
        }

        public void setHits(List<SectionHit> hits) {
            this.hits = hits;
        }

        static public class SectionHit implements Serializable {
            private String sectionUri;
            private String sectionLabel;
            private String sectionMarker;

            public SectionHit() {
            }

            public SectionHit(String sectionUri2, String sectionLabel2, String sectionMarker2) {
                setSectionUri(sectionUri2);
                setSectionLabel(sectionLabel2);
                setSectionMarker(sectionMarker2);
            }

            public String getSectionUri() {
                return sectionUri;
            }

            public void setSectionUri(String sectionUri) {
                this.sectionUri = sectionUri;
            }

            public String getSectionLabel() {
                return sectionLabel;
            }

            public void setSectionLabel(String sectionLabel) {
                this.sectionLabel = sectionLabel;
            }

            public String getSectionMarker() {
                return sectionMarker;
            }

            public void setSectionMarker(String sectionMarker) {
                this.sectionMarker = sectionMarker;
            }
        }
    }
}
