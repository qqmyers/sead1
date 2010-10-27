package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.Map;
import java.util.TreeMap;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class GetAvailableUserMetadataFieldsResult implements Result {
    Map<String, String> availableFields; // name -> uri

    public void addField(String name, String uri) {
        getAvailableFields().put(name, uri);
    }

    public Map<String, String> getAvailableFields() {
        if (availableFields == null) {
            availableFields = new TreeMap<String, String>();
        }
        return availableFields;
    }
}
