package edu.illinois.ncsa.mmdb.web.client.dispatch;

import net.customware.gwt.dispatch.shared.Action;

@SuppressWarnings("serial")
public class DeleteRelationship implements Action<DeleteRelationshipResult> {

    private String uri1;
    private String uri2;
    private String type;
    private String person;

    public DeleteRelationship() {
    }

    public DeleteRelationship(String uri1, String type, String uri2, String person) {
        this.uri1 = uri1;
        this.type = type;
        this.uri2 = uri2;
        this.person = person;
    }

    /**
     * @return dataset's first URI
     */
    public String getUri1() {
        return uri1;
    }

    /**
     * @return dataset 2's URI
     */
    public String getUri2() {
        return uri2;
    }

    /**
     * @return the relationship type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the relationship creator
     */
    public String getCreator() {
        return person;
    }
}
