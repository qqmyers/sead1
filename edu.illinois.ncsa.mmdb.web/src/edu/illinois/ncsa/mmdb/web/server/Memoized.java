package edu.illinois.ncsa.mmdb.web.server;

public abstract class Memoized<T> {
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
			System.out.println("RECOMPUTING BECAUSE "+now+">"+expires+" or "+force); // FIXME debug
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
