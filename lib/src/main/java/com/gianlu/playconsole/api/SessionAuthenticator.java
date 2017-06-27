package com.gianlu.playconsole.api;


import java.util.Map;

import cz.msebera.android.httpclient.client.CookieStore;

public class SessionAuthenticator {
    public final String dev_acc;
    public final Map<String, String> headers;
    public final String xsrfToken;
    public final CookieStore cookieStore;

    public SessionAuthenticator(String dev_acc, Map<String, String> headers, String xsrfToken, CookieStore cookieStore) {
        this.dev_acc = dev_acc;
        this.headers = headers;
        this.xsrfToken = xsrfToken;
        this.cookieStore = cookieStore;
    }

    @Override
    public String toString() {
        return "SessionAuthenticator{" +
                "dev_acc='" + dev_acc + '\'' +
                ", headers=" + headers +
                ", xsrfToken='" + xsrfToken + '\'' +
                ", cookieStore=" + cookieStore +
                '}';
    }
}
