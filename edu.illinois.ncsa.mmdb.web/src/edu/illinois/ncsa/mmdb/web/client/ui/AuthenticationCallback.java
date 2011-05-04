package edu.illinois.ncsa.mmdb.web.client.ui;

public interface AuthenticationCallback {
    void onFailure();

    void onSuccess(String userUri, String sessionKey);
}
