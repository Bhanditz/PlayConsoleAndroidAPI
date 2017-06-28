package com.gianlu.playconsole.api;

import android.support.annotation.NonNull;

import com.gianlu.playconsole.api.Models.SessionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.EntityUtils;

public class PlayConsoleRequester {
    private static PlayConsoleRequester instance;
    private final HttpContext httpContext;
    private final HttpClient client;
    private final SessionInfo info;

    private PlayConsoleRequester(SessionInfo info) {
        this.client = setupHttpClient(info);
        this.httpContext = new BasicHttpContext();
        this.info = info;
    }

    @NonNull
    public static PlayConsoleRequester get(SessionInfo info) {
        if (instance == null || !instance.info.equals(info))
            instance = new PlayConsoleRequester(info);
        return instance;
    }

    public static CloseableHttpClient setupHttpClient(SessionInfo info) {
        return HttpClientBuilder.create().setDefaultCookieStore(info.cookieStore).build();
    }

    public JSONObject execute(PlayConsoleRequest request) throws JSONException, NetworkException, IOException {
        return execute(request.request, request.body);
    }

    private JSONObject execute(HttpPost request, JSONObject payload) throws IOException, NetworkException, JSONException {
        setPayloadXsrf(payload);
        request.setEntity(new ByteArrayEntity(payload.toString().getBytes()));
        authenticateRequest(request);

        HttpResponse resp = client.execute(request, httpContext);
        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != 200) throw new NetworkException(sl);

        return new JSONObject(EntityUtils.toString(resp.getEntity(), Charset.forName("UTF-8")));
    }

    private void setPayloadXsrf(JSONObject request) throws JSONException {
        request.put("xsrf", info.startupData.xsrfToken);
    }

    private void authenticateRequest(HttpRequestBase request) {
        request.addHeader("X-Gwt-Module-Base", info.xGwtModuleBase);
        request.addHeader("X-Gwt-Permutation", info.xGwtPermutation);
        request.addHeader("Content-Type", SessionInfo.contentType);
    }
}
