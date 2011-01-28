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
package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetUserMetadata extends SubjectAction<EmptyResult> {
    /**
	 * 
	 */
    private static final long serialVersionUID = -6257131114264143785L;

    String                    propertyUri;
    boolean                   isUriType;

    public SetUserMetadata() {
    }

    private Collection<String> values;

    public SetUserMetadata(String uri, String propertyUri, Collection<String> values) {
        this(uri, propertyUri, values, false);
    }

    public SetUserMetadata(String uri, String propertyUri, Collection<String> values, boolean isUriType) {
        this.uri = uri;
        setPropertyUri(propertyUri);
        setUriType(isUriType);
        this.setValues(values);
    }

    public SetUserMetadata(String uri, String propertyUri, String value) {
        this(uri, propertyUri, value, false);
    }

    public SetUserMetadata(String uri, String propertyUri, String value, boolean isUriType) {
        this.uri = uri;
        setPropertyUri(propertyUri);
        Set<String> values = new HashSet<String>();
        values.add(value);
        this.setValues(values);
        setUriType(isUriType);
    }

    public void setValues(Collection<String> values) {
        this.values = values;
    }

    public void setValues(Collection<String> values, boolean isUriType) {
        this.values = values;
        setUriType(isUriType);
    }

    public Collection<String> getValues() {
        return values;
    }

    public String getPropertyUri() {
        return propertyUri;
    }

    public void setPropertyUri(String propertyUri) {
        this.propertyUri = propertyUri;
    }

    public boolean isUriType() {
        return isUriType;
    }

    public void setUriType(boolean isUriType) {
        this.isUriType = isUriType;
    }
}
