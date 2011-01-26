package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.List;

import net.customware.gwt.dispatch.shared.Result;

public class GetAllowedValuesResult implements Result {
    List<NamedThing> allowedValues;

    public List<NamedThing> getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(List<NamedThing> allowedValues) {
        this.allowedValues = allowedValues;
    }

}
