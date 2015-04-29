
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
package org.sead.acr.common.utilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This is a generic abstract memoization utility.
 * To implement, provide an implementation of computeValue.
 * To use, construct an instance and set the time-to-live (ttl).
 * Then call getValue. If the expiration time has passed, computeValue
 * will be called to retrieve the current value. Otherwise the
 * cached value will be returned.
 * Changing the ttl will not affect the expiration time until
 * computeValue is called.
 *
 * @param <T>
 */
public abstract class Memoized<T> {
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(Memoized.class);
	
	T cachedValue;
	boolean forceOnNull = false;
	long expires;
	long ttl;
	
	public Memoized() { }
	
	public Memoized(long ttl) {
		this.ttl = ttl;
	}
	
	public T getValue() {
		return getValue(false);
	}
	public T getValue(boolean force) {
		long now = System.currentTimeMillis();
		if(now > expires || force || (cachedValue == null && forceOnNull)) {
			cachedValue = computeValue();
			expires = now + ttl;
		}
		return cachedValue;
	}
	
	public abstract T computeValue();

	public long getTtl() {
		return ttl;
	}

	public void setTtl(long ttl) {
		this.ttl = ttl;
	}

	public boolean isForceOnNull() {
		return forceOnNull;
	}

	public void setForceOnNull(boolean forceOnNull) {
		this.forceOnNull = forceOnNull;
	}
}
