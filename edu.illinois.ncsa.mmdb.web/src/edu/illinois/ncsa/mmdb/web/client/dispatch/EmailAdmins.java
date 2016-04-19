package edu.illinois.ncsa.mmdb.web.client.dispatch;

@SuppressWarnings("serial")
public class EmailAdmins extends AuthorizedAction<EmptyResult> {

    String messageString;

    public EmailAdmins() {
    }

    public EmailAdmins(String text, String user) {
        messageString = text;
        setUser(user);
    }

    public String getMessage() {
        return messageString;
    }

}
