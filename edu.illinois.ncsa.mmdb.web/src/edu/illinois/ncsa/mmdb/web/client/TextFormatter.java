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
package edu.illinois.ncsa.mmdb.web.client;

/**
 * Static methods to format text.
 * 
 * @author Luigi Marini
 * 
 */
public class TextFormatter {

	/**
	 * Format bytes.
	 * 
	 * @param x
	 *            number of bytes
	 * @return formatted string
	 */
	public static String humanBytes(long x) {
		if (x == Integer.MAX_VALUE) {
			return "No limit";
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

    public static String escapeEmailAddress(String emailAddress) {
        return emailAddress.replaceAll("@", "_at_").replaceAll("\\.", "_dot_");
    }

    public static String unescapeEmailAddress(String emailAddress) {
        return emailAddress.replaceAll("_at_", "@").replaceAll("_dot_", ".");
    }

}
