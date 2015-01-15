package edu.illinois.ncsa.mmdb.web.client.dispatch;

import edu.illinois.ncsa.mmdb.web.client.dispatch.EditRole.ActionType;

public class EditUserRetirement extends AuthorizedAction<EmptyResult> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String            targetUser;
    private ActionType        type;

    public EditUserRetirement() {
    }

    public EditUserRetirement(String userUri, ActionType type) {
        setTargetUser(userUri);
        setType(type);
    }

    public String getTargetUser() {
        return targetUser;
    }

    public void setTargetUser(String targetUser) {
        this.targetUser = targetUser;
    }

    public ActionType getType() {
        return type;
    }

    public void setType(ActionType type) {
        this.type = type;
    }

}
