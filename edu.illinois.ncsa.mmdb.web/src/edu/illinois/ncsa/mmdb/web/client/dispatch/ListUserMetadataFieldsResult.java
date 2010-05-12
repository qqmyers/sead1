package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Map;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class ListUserMetadataFieldsResult implements Result {
    Map<String, String> fieldLabels;

    public Map<String, String> getFieldLabels() {
        return fieldLabels;
    }

    public void setFieldLabels(Map<String, String> fieldLabels) {
        this.fieldLabels = fieldLabels;
    }
}
