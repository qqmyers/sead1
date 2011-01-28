package edu.illinois.ncsa.mmdb.web.client.dispatch;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import net.customware.gwt.dispatch.shared.Result;

@SuppressWarnings("serial")
public class ListUserMetadataFieldsResult implements Result {
    List<UserMetadataField> fields;

    public SortedSet<UserMetadataField> getFieldsSortedByName() {
        SortedSet<UserMetadataField> result = new TreeSet<UserMetadataField>(new Comparator<UserMetadataField>() {
            @Override
            public int compare(UserMetadataField k1, UserMetadataField k2) {
                int c = k1.getLabel().compareTo(k2.getLabel());
                if (c == 0) {
                    return k1.getUri().compareTo(k2.getUri());
                } else {
                    return c;
                }
            }
        });
        result.addAll(fields);
        return result;
    }

    public void addField(UserMetadataField field) {
        if (fields == null) {
            fields = new ArrayList<UserMetadataField>();
        }
        fields.add(field);
    }

    public List<UserMetadataField> getFields() {
        return fields;
    }

    public void setFields(List<UserMetadataField> fields) {
        this.fields = fields;
    }
}
