package edu.illinois.ncsa.mmdb.web.server;

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
