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
package edu.illinois.ncsa.mmdb.web.client.presenter;

import java.util.TreeSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFields;
import edu.illinois.ncsa.mmdb.web.client.dispatch.ListUserMetadataFieldsResult;
import edu.illinois.ncsa.mmdb.web.client.dispatch.MyDispatchAsync;
import edu.illinois.ncsa.mmdb.web.client.mvp.Presenter;

public class UserMetadataPresenter implements Presenter {
    protected final MyDispatchAsync dispatch;
    protected final Display         display;

    public UserMetadataPresenter(MyDispatchAsync dispatch, Display display) {
        this.dispatch = dispatch;
        this.display = display;
    }

    @Override
    public void bind() {
        dispatch.execute(new ListUserMetadataFields(),
                new AsyncCallback<ListUserMetadataFieldsResult>() {
            public void onFailure(Throwable caught) {
            }

            public void onSuccess(ListUserMetadataFieldsResult result) {
                TreeSet<String> sortedUris = new TreeSet<String>();
                sortedUris.addAll(result.getFieldLabels().keySet());
                for (String key : sortedUris ) {
                    String predicate = key;
                    String label = result.getFieldLabels().get(key);
                    display.addMetadataField(predicate, label);
                }
            }
        });
    }

    public interface Display {
        /**
         * Indicate to the display the name and URI of a user metadata predicate
         */
        void addMetadataField(String uri, String name);

        /**
         * Indicate to the display that a given user metadata predicate has a
         * given value (for whatever content the presenter is presenting)
         */
        void addMetadataValue(String uri, String value);
    }
}
