package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Collection;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;

public class GetUserMetadataFieldsResult implements Result {
	Map<String,String> fieldLabels;
	Map<String,Collection<String>> values;
	
	public Map<String, String> getFieldLabels() {
		return fieldLabels;
	}
	public void setFieldLabels(Map<String, String> fieldLabels) {
		this.fieldLabels = fieldLabels;
	}
	public Map<String, Collection<String>> getValues() {
		return values;
	}
	public void setValues(Map<String, Collection<String>> values) {
		this.values = values;
	}

}
