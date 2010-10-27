package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class CreateRole extends AuthorizedAction<SubjectResult> {
    String name;

    public CreateRole() {
    }

    public CreateRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
