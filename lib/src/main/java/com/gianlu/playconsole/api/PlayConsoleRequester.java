package com.gianlu.playconsole.api;

import com.gianlu.playconsole.api.Models.SessionInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;

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

    public static CloseableHttpClient setupHttpClient(SessionInfo authenticator) {
        return HttpClientBuilder.create().setDefaultCookieStore(authenticator.cookieStore).build();
    }

    public static JSONObject doRequestSync(String url, JSONObject request, SessionInfo authenticator) throws JSONException, IOException, NetworkException {
        HttpPost post = new HttpPost(url);
        authenticateRequest(authenticator, post);
        setRequestXsrf(request, authenticator);
        return doRequestSync(setupHttpClient(authenticator), post, request);
    }

    public static JSONObject doRequestSync(CloseableHttpClient client, HttpPost request, JSONObject payload) throws IOException, NetworkException, JSONException {
        request.setEntity(new ByteArrayEntity(payload.toString().getBytes()));

        HttpResponse resp = client.execute(request, httpContext);
        StatusLine sl = resp.getStatusLine();
        if (sl.getStatusCode() != 200) throw new NetworkException(sl);

        return new JSONObject(EntityUtils.toString(resp.getEntity(), Charset.forName("UTF-8")));
    }

    public static void setRequestXsrf(JSONObject request, SessionInfo authenticator) throws JSONException {
        request.put("xsrf", authenticator.startupData.xsrfToken);
    }

    public static void authenticateRequest(SessionInfo authenticator, HttpRequestBase request) {
        request.addHeader("X-Gwt-Module-Base", authenticator.xGwtModuleBase);
        request.addHeader("X-Gwt-Permutation", authenticator.xGwtPermutation);
        request.addHeader("Content-Type", SessionInfo.contentType);
    }
}
