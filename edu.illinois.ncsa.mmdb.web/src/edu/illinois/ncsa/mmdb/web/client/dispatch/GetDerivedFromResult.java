package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;
import edu.uiuc.ncsa.cet.bean.DatasetBean;

@SuppressWarnings("serial")
public class GetDerivedFromResult implements Result {
	List<DatasetBean> derivedFrom;

	public GetDerivedFromResult() { }
	
	public GetDerivedFromResult(List<DatasetBean> derivedFrom) {
		setDerivedFrom(derivedFrom);
	}
	
	public List<DatasetBean> getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(List<DatasetBean> derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
	
	
}
