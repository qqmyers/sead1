package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class DefaultTheRole extends SubjectAction<EmptyResult> {
    public DefaultTheRole() {
    }

    public DefaultTheRole(String roleUri) {
        setUri(roleUri);
    }
}
