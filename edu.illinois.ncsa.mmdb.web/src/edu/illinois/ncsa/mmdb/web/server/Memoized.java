package edu.illinois.ncsa.mmdb.web.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * FIXME Add comments
 *
 *
 * @param <T>
 */
public abstract class Memoized<T> {
	
	/** Commons logging **/
	private static Log log = LogFactory.getLog(Memoized.class);
	
	T cachedValue;
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
		if(now > expires || force) {
			log.debug("RECOMPUTING BECAUSE "+now+" > " + expires + " or " + force);
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
}
