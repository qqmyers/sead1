package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class DeleteRole extends SubjectAction<EmptyResult> {
    public DeleteRole() {
    }

    public DeleteRole(String roleUri) {
        setUri(roleUri);
    }
}
