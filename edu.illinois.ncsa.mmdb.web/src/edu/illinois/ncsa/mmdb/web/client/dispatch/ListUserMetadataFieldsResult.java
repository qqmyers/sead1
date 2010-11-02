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

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class ListUserMetadataFieldsResult implements Result {
    Map<String, String> fields; // uri -> label, sorted alpha by label 

    public Map<String, String> getFieldLabels() {
        if (fields == null) {
            fields = new TreeMap<String, String>(new Comparator<String>() {
                @Override
                public int compare(String arg0, String arg1) {
                    return fields.get(arg0).compareTo(fields.get(arg1));
                }
            });
        }
        return fields;
    }

    public SortedMap<String, String> getFieldsOrderedByLabel() {
        getFieldLabels();
        SortedMap<String, String> result = new TreeMap<String, String>(new Comparator<String>() {
            @Override
            public int compare(String arg0, String arg1) {
                return fields.get(arg0).compareTo(fields.get(arg1));
            }
        });
        result.putAll(fields);
        return result;
    }

    public void setFieldLabels(Map<String, String> fieldLabels) {
        this.fields = fieldLabels;
    }

    public void addField(String name, String uri) {
        getFieldLabels().put(name, uri);
    }
}
