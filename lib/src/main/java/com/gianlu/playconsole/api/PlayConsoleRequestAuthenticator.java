package com.gianlu.playconsole.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;

public class PlayConsoleRequestAuthenticator {

    public static CloseableHttpClient setupHttpClient(SessionAuthenticator authenticator) {
        return HttpClientBuilder.create().setDefaultCookieStore(authenticator.cookieStore).build();
    }

    public static JSONObject createRequestPayload(SessionAuthenticator authenticator, String method, JSONObject params) throws JSONException {
        return new JSONObject().put("xsrf", authenticator).put("method", method).put("params", params);
    }

    public static void authenticateRequest(SessionAuthenticator authenticator, HttpRequestBase request) {
        for (Map.Entry<String, String> header : authenticator.headers.entrySet())
            request.addHeader(header.getKey(), header.getValue());
    }
}
