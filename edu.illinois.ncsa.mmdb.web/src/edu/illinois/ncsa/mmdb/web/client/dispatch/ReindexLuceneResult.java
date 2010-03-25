package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class ReindexLuceneResult implements Result {
	int numberQueued = 0;

	public ReindexLuceneResult() { }
	
	public ReindexLuceneResult(int nq) {
		setNumberQueued(nq);
	}

	public int getNumberQueued() {
		return numberQueued;
	}

	public void setNumberQueued(int numberQueued) {
		this.numberQueued = numberQueued;
	}
	
}
