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
package edu.illinois.ncsa.mmdb.web.client;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.uiuc.ncsa.cet.bean.PersonBean;

/**
 * Holds information about the current state of the user's session.
 * Non-persistent upon login/logout.
 *
 * @author futrelle
 */
public class UserSessionState {
    private Map<String, String> preferences;
    private String              sessionKey;
    private Set<String>         selectedDatasets;
    private PersonBean          currentUser;

    public Map<String, String> getPreferences() {
        if (preferences == null) {
            preferences = new HashMap<String, String>();
            initializePreferences();
        }
        return preferences;
    }

    public void setPreferences(Map<String, String> preferences) {
        this.preferences = preferences;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public Set<String> getSelectedDatasets() {
        if (selectedDatasets == null) {
            selectedDatasets = new HashSet<String>();
        }
        return selectedDatasets;
    }

    public void setSelectedDatasets(Set<String> selectedDatasets) {
        this.selectedDatasets = selectedDatasets;
    }

    private void initializePreferences() {
        preferences.put(MMDB.DATASET_VIEW_TYPE_PREFERENCE, PagingDcThingView.GRID_VIEW_TYPE);
        preferences.put(MMDB.COLLECTION_VIEW_TYPE_PREFERENCE, PagingDcThingView.LIST_VIEW_TYPE);
    }

    public void setCurrentUser(PersonBean currentUser) {
        this.currentUser = currentUser;
    }

    public PersonBean getCurrentUser() {
        return currentUser;
    }
}
