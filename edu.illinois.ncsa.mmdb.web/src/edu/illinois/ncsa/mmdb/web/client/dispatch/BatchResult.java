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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class BatchResult implements Result {
    String              actionVerb = "modified";
    Set<String>         successes  = new HashSet<String>();        // uri's of sucessful ops
    Map<String, String> failures   = new HashMap<String, String>(); // uri -> message explaining why the op failed for that dataset

    public BatchResult() {
    }

    public BatchResult(String actionVerb) {
        this.actionVerb = actionVerb;
    }

    public void addSuccess(String uri) {
        successes.add(uri);
    }

    public void addSuccesses(Collection<String> batch) {
        successes.addAll(batch);
    }

    public Set<String> getSuccesses() {
        return successes;
    }

    public void setFailure(String uri, String message) {
        failures.put(uri, message);
    }

    public void setFailure(Collection<String> batch, String message) {
        for (String uri : batch ) {
            setFailure(uri, message);
        }
    }

    /** put a default message in based on the given exception */
    public void setFailure(String uri, Throwable exception) {
        failures.put(uri, "failed: " + exception.getMessage());
    }

    public void setFailure(Collection<String> batch, Throwable exception) {
        for (String uri : batch ) {
            setFailure(uri, exception);
        }
    }

    public String getFailure(String uri) {
        return failures.get(uri);
    }

    public Map<String, String> getFailures() {
        return failures;
    }

    public String getActionVerb() {
        return actionVerb;
    }
}
