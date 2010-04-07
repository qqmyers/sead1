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
package edu.illinois.ncsa.mmdb.web.rest;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.tupeloproject.rdf.Resource;
import org.tupeloproject.rdf.terms.Dc;
import org.tupeloproject.rdf.terms.Rdf;
import org.tupeloproject.util.Base64;

public class RestUriMinter {
	static RestUriMinter singleton;
	MessageDigest messageDigest = null;
	String seed = "";
	
	public static RestUriMinter getInstance() {
		if(singleton == null) {
			singleton = new RestUriMinter();
		}
		return singleton;
	}
	
	public String mintUri(Map<Resource,Object> metadata) {
		if(messageDigest == null) {
			try {
				messageDigest = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				// this should not happen
				e.printStackTrace();
			}
		}
		String prefix = "data";
		if(metadata != null) {
			if(RestService.IMAGE_TYPE.equals(metadata.get(Rdf.TYPE))) {
				prefix = "img";
			}
			if(metadata.containsKey(RestService.FORMAT_PROPERTY) &&
				metadata.get(RestService.FORMAT_PROPERTY).toString().startsWith("image/")) {
				prefix = "img";
			}
			if(RestService.COLLECTION_TYPE.equals(metadata.get(Rdf.TYPE))) {
				prefix = "col";
			}
		}
		seed += System.currentTimeMillis() + Runtime.getRuntime().freeMemory();
		try {
			byte[] md = messageDigest.digest(seed.getBytes("US-ASCII"));
			String b64 = Base64.encodeBytes(md).replaceAll("/","_").replaceAll("\\+","-").replaceFirst("=*$","");
			seed = b64;
			return "tag:medici@uiuc.edu,2009:"+prefix+"_" + b64;
		} catch (UnsupportedEncodingException e) {
			// this should not happen
			e.printStackTrace();
			return null;
		}
	}
}
