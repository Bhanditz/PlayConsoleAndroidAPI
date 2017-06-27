package com.gianlu.playconsole.api;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.StatusLine;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.methods.HttpRequestBase;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.client.CloseableHttpClient;
import cz.msebera.android.httpclient.impl.client.HttpClientBuilder;
import cz.msebera.android.httpclient.protocol.BasicHttpContext;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.EntityUtils;

public class PlayConsoleRequester {
    public static final HttpContext httpContext = new BasicHttpContext();

    public static CloseableHttpClient setupHttpClient(SessionAuthenticator authenticator) {
        return HttpClientBuilder.create().setDefaultCookieStore(authenticator.cookieStore).build();
    }

    public static JSONObject doRequestSync(String url, JSONObject request, SessionAuthenticator authenticator) throws JSONException, IOException, GeneralException {
        HttpPost post = new HttpPost(url);
        authenticateRequest(authenticator, post);
        setRequestXsrf(request, authenticator);
        return doRequestSync(setupHttpClient(authenticator), post, request);
    }

    public static JSONObject doRequestSync(CloseableHttpClient client, HttpPost request, JSONObject payload) throws IOException, GeneralException, JSONException {
        request.setEntity(new ByteArrayEntity(payload.toString().getBytes()));

        HttpResponse resp = client.execute(request, httpContext);
        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != 200) throw new GeneralException(sl);

        return new JSONObject(EntityUtils.toString(resp.getEntity(), Charset.forName("UTF-8")));
    }

    public static void setRequestXsrf(JSONObject request, SessionAuthenticator authenticator) throws JSONException {
        request.put("xsrf", authenticator.xsrfToken);
    }

    public static void authenticateRequest(SessionAuthenticator authenticator, HttpRequestBase request) {
        for (Map.Entry<String, String> header : authenticator.headers.entrySet())
            request.addHeader(header.getKey(), header.getValue());
    }
}
