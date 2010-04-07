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
/**
 * 
 */
package edu.illinois.ncsa.mmdb.web.client.place;

import java.util.HashMap;
import java.util.Map;

/**
 * @author lmarini
 *
 */
public class PlaceRequest {

	private static final String PARAMETERS_SPLIT = "?";

	private static final String PARAMETER_ASSIGNMENT = "=";

	private final Place id;
	
    private final Map<String, String> params;
	
    public PlaceRequest(Place id, Map<String, String> params) {
		this.id = id;
		this.params = params;
	}

	public PlaceRequest(String value) {
		int split = value.indexOf(PARAMETERS_SPLIT);
		id = new Place(value.substring(0, split));
		String[] paramsString = value.substring(split+1).split(PARAMETER_ASSIGNMENT);
		params = new HashMap<String, String>();
		for (int i=0; i < paramsString.length; i++) {
			params.put(paramsString[i], paramsString[i+1]);
		}
	}

	public Map<String, String> getParams() {
		return params;
	}

	public String getParameter(String id) {
		return params.get(id);
	}

	public Place getId() {
		return id;
	}
    
}
